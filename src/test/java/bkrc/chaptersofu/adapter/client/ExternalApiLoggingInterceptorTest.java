package bkrc.chaptersofu.adapter.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class ExternalApiLoggingInterceptorTest {

    @Test
    void 외부_API_성공_상태와_소요시간을_기록하고_query는_숨긴다(CapturedOutput output) throws Exception {
        HttpRequest request = request("https://example.com/v1/books?ttbkey=secret-key", HttpMethod.GET);
        ClientHttpResponse response = response(HttpStatus.OK);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(eq(request), any(byte[].class))).thenReturn(response);

        new ExternalApiLoggingInterceptor("aladin").intercept(request, new byte[0], execution);

        assertThat(output.getOut())
                .contains("EXTERNAL_API_COMPLETED")
                .contains("service=aladin")
                .contains("method=GET")
                .contains("path=/v1/books")
                .contains("status=200")
                .contains("durationMs=")
                .doesNotContain("ttbkey")
                .doesNotContain("secret-key");
    }

    @Test
    void 재시도_가능한_HTTP_오류를_WARN으로_기록한다(CapturedOutput output) throws Exception {
        HttpRequest request = request("https://api.openai.com/v1/chat/completions", HttpMethod.POST);
        ClientHttpResponse response = response(HttpStatus.TOO_MANY_REQUESTS);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(eq(request), any(byte[].class))).thenReturn(response);

        new ExternalApiLoggingInterceptor("gpt").intercept(request, new byte[0], execution);

        assertThat(output.getOut())
                .contains("EXTERNAL_API_FAILED")
                .contains("service=gpt")
                .contains("status=429")
                .contains("retryable=true");
    }

    @Test
    void 네트워크_예외의_종류와_소요시간을_기록한_뒤_다시_던진다(CapturedOutput output) throws Exception {
        HttpRequest request = request("https://example.com/v1/books", HttpMethod.GET);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(eq(request), any(byte[].class))).thenThrow(new IOException("connection reset"));
        ExternalApiLoggingInterceptor interceptor = new ExternalApiLoggingInterceptor("aladin");

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(IOException.class)
                .hasMessage("connection reset");

        assertThat(output.getOut())
                .contains("EXTERNAL_API_FAILED")
                .contains("service=aladin")
                .contains("status=unavailable")
                .contains("retryable=true")
                .contains("exceptionType=IOException")
                .contains("durationMs=");
    }

    private HttpRequest request(String uri, HttpMethod method) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(uri));
        when(request.getMethod()).thenReturn(method);
        return request;
    }

    private ClientHttpResponse response(HttpStatus status) throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(status);
        return response;
    }
}
