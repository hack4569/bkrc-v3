package com.bkrc.bkrcv3.member.security;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.member.application.UserService;
import com.bkrc.bkrcv3.member.application.request.LoginForm;
import com.bkrc.bkrcv3.member.application.response.LoginResponse;
import com.bkrc.bkrcv3.member.dto.MemberDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private UserService userService;
    private Environment environment;
    private ObjectMapper objectMapper;
    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserService userService,
                                Environment environment,
                                ObjectMapper objectMapper) {
        super(authenticationManager);
        this.userService = userService;
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            // 요청 바디를 파싱할 때는 필드로 주입된 ObjectMapper를 사용하고,
            // 실패 시 form 파라미터에서 읽어오는 폴백을 둡니다.
            LoginForm creds = null;
            try {
                creds = this.objectMapper.readValue(req.getInputStream(), LoginForm.class);
            } catch (Exception e) {
                // JSON 파싱 실패 시 로그와 함께 form 파라미터에서 값을 꺼내 시도
                log.debug("attemptAuthentication: failed to parse JSON body, contentType={}; error={}", req.getContentType(), e.toString());
                String loginId = req.getParameter("loginId");
                String password = req.getParameter("password");
                if (loginId != null && password != null) {
                    creds = new LoginForm(loginId, password, false);
                } else {
                    throw e; // 둘 다 없으면 원래 예외를 처리하기 위해 던짐
                }
            }

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getLoginId(),
                            creds.getPassword(),
                            new ArrayList<>()
                    )
            );

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        String userName = ((User) authResult.getPrincipal()).getUsername();
        MemberDto memberDto = userService.getMemberByLoginId(userName);

        byte[] secretKeyBytes = environment.getProperty("token.secret").getBytes(StandardCharsets.UTF_8);

        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        Instant now = Instant.now();

        String token = Jwts.builder()
                .subject(memberDto.getLoginId())
                .expiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration-time")))))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();

        // 로그에 토큰 생성 여부를 남겨 디버깅에 도움을 줌
        log.info("successfulAuthentication - generated token for loginId={}", memberDto.getLoginId());

        res.setContentType("application/json; charset=UTF-8");
        // 실제 토큰과 로그인 ID를 담은 응답 인스턴스를 반환하도록 수정
        objectMapper.writeValue(res.getWriter(), new LoginResponse(token, memberDto.getLoginId()));
    }
}
