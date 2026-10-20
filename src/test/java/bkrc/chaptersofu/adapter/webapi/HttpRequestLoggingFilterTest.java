package bkrc.chaptersofu.adapter.webapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class HttpRequestLoggingFilterTest {

    private final HttpRequestLoggingFilter filter = new HttpRequestLoggingFilter();

    @Test
    void 전달받은_traceId와_HTTP_요청_응답_정보를_로그에_기록한다(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/member/10");
        request.addHeader(HttpRequestLoggingFilter.TRACE_ID_HEADER, "client-trace-123");
        request.setQueryString("password=must-not-be-logged");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> traceIdInsideRequest = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            traceIdInsideRequest.set(MDC.get("traceId"));
            ((MockHttpServletResponse) res).setStatus(201);
        });

        assertThat(traceIdInsideRequest).hasValue("client-trace-123");
        assertThat(response.getHeader(HttpRequestLoggingFilter.TRACE_ID_HEADER))
                .isEqualTo("client-trace-123");
        assertThat(output.getOut())
                .contains("HTTP_REQUEST")
                .contains("traceId=client-trace-123")
                .contains("method=GET")
                .contains("uri=/v1/member/10")
                .contains("status=201")
                .contains("durationMs=")
                .doesNotContain("password")
                .doesNotContain("must-not-be-logged");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void traceId가_없거나_안전하지_않으면_새로_생성한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/history");
        request.addHeader(HttpRequestLoggingFilter.TRACE_ID_HEADER, "invalid\ntrace");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(HttpRequestLoggingFilter.TRACE_ID_HEADER))
                .matches("[0-9a-f-]{36}")
                .isNotEqualTo("invalid\ntrace");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void actuator_요청은_로그에서_제외한다(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(output.getOut()).doesNotContain("HTTP_REQUEST");
    }
}
