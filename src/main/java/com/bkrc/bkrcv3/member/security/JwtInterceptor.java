package com.bkrc.bkrcv3.member.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    public static final String LOGIN_ID_KEY = "loginId";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String authorizationHeader = request.getHeader("Authorization");

        // 토큰 없음 → 401
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"로그인이 필요합니다.\"}");
            return false;
        }

        String token = authorizationHeader.substring(7);

        // 토큰 유효하지 않음 → 401
        if (!jwtProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"유효하지 않은 토큰입니다.\"}");
            return false;
        }

        // 토큰에서 loginId 추출 → request에 저장
        String loginId = jwtProvider.extractLoginId(token);
        request.setAttribute(LOGIN_ID_KEY, loginId);
        log.debug("[JWT] 인증 성공 loginId={}", loginId);

        return true;
    }
}
