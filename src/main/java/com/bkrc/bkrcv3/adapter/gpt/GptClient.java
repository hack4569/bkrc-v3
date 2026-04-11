package com.bkrc.bkrcv3.adapter.gpt;

import com.bkrc.bkrcv3.adapter.gpt.request.GptRequest;
import com.bkrc.bkrcv3.adapter.gpt.response.GptResponse;
import com.bkrc.bkrcv3.required.AiClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    @PostConstruct
    void initRestClient() {
        this.gptClient = RestClient.create(gptHost);
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
