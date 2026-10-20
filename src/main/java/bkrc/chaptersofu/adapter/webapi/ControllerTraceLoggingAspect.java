package bkrc.chaptersofu.adapter.webapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** 모든 RestController 호출의 진입과 종료를 민감정보가 제거된 TRACE 로그로 기록합니다. */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerTraceLoggingAspect {

    private final ControllerArgumentSanitizer argumentSanitizer;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object traceController(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isTraceEnabled()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String controller = signature.getDeclaringType().getSimpleName();
        String handler = signature.getMethod().getName();
        String arguments = argumentSanitizer.sanitize(signature.getParameterNames(), joinPoint.getArgs());
        long startedAt = System.nanoTime();

        log.trace(
                "CONTROLLER_REQUEST controller={} handler={} arguments={}",
                controller,
                handler,
                arguments
        );

        try {
            Object result = joinPoint.proceed();
            log.trace(
                    "CONTROLLER_RESPONSE controller={} handler={} resultType={} durationMs={}",
                    controller,
                    handler,
                    result == null ? "void" : result.getClass().getSimpleName(),
                    elapsedMillis(startedAt)
            );
            return result;
        } catch (Throwable exception) {
            // 예외 메시지에는 사용자 입력이 섞일 수 있어 타입만 기록하고 실제 내용은 전역 예외 로그에 맡깁니다.
            log.trace(
                    "CONTROLLER_EXCEPTION controller={} handler={} exceptionType={} durationMs={}",
                    controller,
                    handler,
                    exception.getClass().getSimpleName(),
                    elapsedMillis(startedAt)
            );
            throw exception;
        }
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
