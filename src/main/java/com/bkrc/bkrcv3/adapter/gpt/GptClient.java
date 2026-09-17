package com.bkrc.bkrcv3.adapter.gpt;

import com.bkrc.bkrcv3.adapter.gpt.request.GptRequest;
import com.bkrc.bkrcv3.adapter.gpt.response.GptResponse;
import com.bkrc.bkrcv3.required.AiClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GptClient implements AiClient<GptRequest, GptResponse> {
    private RestClient gptClient;
    @Value("${gpt.host}")
    public String gptHost;
    @Value("${gpt.api_key}")
    private String gptApiKey;
    @Value("${external-api.gpt.connect-timeout:2s}")
    private java.time.Duration connectTimeout;
    @Value("${external-api.gpt.read-timeout:20s}")
    private java.time.Duration readTimeout;

    @PostConstruct
    void initRestClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.gptClient = RestClient.builder()
                .baseUrl(gptHost)
                .requestFactory(requestFactory)
                .build();
    }

    public GptResponse getChatResponse(GptRequest gptRequest) {
        ResponseEntity<GptResponse> response = gptClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + gptApiKey)
                .header("Content-Type", "application/json")
                .body(gptRequest)
                .retrieve()
                .toEntity(GptResponse.class);
        return response.getBody();
    }
}
