package com.bkrc.bkrcv3.member.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    @Value("${token.secret}")
    private String secret;

    @Value("${token.expiration-time}")
    private long expirationTime;

    public String generateToken(String loginId) {
        SecretKey secretKey = getSecretKey();
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(loginId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationTime)))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 loginId 추출
    public String extractLoginId(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰");
            return false;
        } catch (JwtException e) {
            log.warn("[JWT] 유효하지 않은 토큰");
            return false;
        } catch (Exception e) {
            log.error("[JWT] 토큰 처리 중 예외 발생", e);
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
