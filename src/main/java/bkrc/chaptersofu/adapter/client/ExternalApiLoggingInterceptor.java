package bkrc.chaptersofu.adapter.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 외부 API 호출 결과를 공통 형식으로 기록합니다.
 * API key나 사용자 검색어가 포함될 수 있는 query string과 요청/응답 본문은 기록하지 않습니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ExternalApiLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final String service;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        long startedAt = System.nanoTime();

        try {
            ClientHttpResponse response = execution.execute(request, body);
            int status = response.getStatusCode().value();
            long durationMs = elapsedMillis(startedAt);

            if (response.getStatusCode().isError()) {
                log.warn(
                        "EXTERNAL_API_FAILED service={} method={} path={} status={} durationMs={} retryable={}",
                        service,
                        request.getMethod(),
                        request.getURI().getPath(),
                        status,
                        durationMs,
                        isRetryable(status)
                );
            } else {
                log.info(
                        "EXTERNAL_API_COMPLETED service={} method={} path={} status={} durationMs={}",
                        service,
                        request.getMethod(),
                        request.getURI().getPath(),
                        status,
                        durationMs
                );
            }
            return response;
        } catch (IOException | RuntimeException exception) {
            log.warn(
                    "EXTERNAL_API_FAILED service={} method={} path={} status=unavailable durationMs={} "
                            + "retryable=true exceptionType={} message={}",
                    service,
                    request.getMethod(),
                    request.getURI().getPath(),
                    elapsedMillis(startedAt),
                    exception.getClass().getSimpleName(),
                    exception.getMessage()
            );
            throw exception;
        }
    }

    private static long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    private static boolean isRetryable(int status) {
        return status == 429 || status >= 500;
    }
}
