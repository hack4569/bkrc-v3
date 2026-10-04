package com.bkrc.bkrcv3.member.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    /** 완성된 JwtParser는 불변 객체이므로 요청마다 만들지 않고 모든 요청에서 재사용합니다. */
    private final JwtParser jwtParser;

    public JwtAuthorizationFilter(Environment environment) {
        byte[] secretKeyBytes = environment.getRequiredProperty("token.secret")
                .getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = authorizationHeader.substring(7);
            Claims claims = jwtParser.parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long memberId = Long.parseLong(subject);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(memberId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰 요청 uri={}", request.getRequestURI());
        } catch (JwtException e) {
            log.warn("[JWT] 유효하지 않은 토큰 요청 uri={}", request.getRequestURI());
        } catch (Exception e) {
            log.error("[JWT] 토큰 처리 중 예외 발생", e);
        }
        filterChain.doFilter(request, response);
    }
}
