package bkrc.chaptersofu.adapter.webapi;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HTTP 요청이 완료되면 요청과 응답의 핵심 정보를 한 줄로 기록합니다.
 * 본문, query string, Authorization 헤더는 개인정보와 비밀 값 노출을 막기 위해 기록하지 않습니다.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final Pattern SAFE_TRACE_ID = Pattern.compile("[A-Za-z0-9._-]{1,100}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        long startedAt = System.nanoTime();
        response.setHeader(TRACE_ID_HEADER, traceId);

        MDC.put(TRACE_ID_MDC_KEY, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            try {
                log.info(
                        "HTTP_REQUEST traceId={} method={} uri={} status={} durationMs={}",
                        traceId,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        durationMs
                );
            } finally {
                // Servlet 스레드가 재사용될 때 이전 요청의 traceId가 섞이지 않도록 반드시 제거합니다.
                MDC.remove(TRACE_ID_MDC_KEY);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 헬스 체크와 메트릭 수집 요청이 비즈니스 요청 로그를 과도하게 만들지 않도록 제외합니다.
        return request.getRequestURI().startsWith("/actuator/");
    }

    private String resolveTraceId(String requestedTraceId) {
        if (requestedTraceId != null && SAFE_TRACE_ID.matcher(requestedTraceId).matches()) {
            return requestedTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
