package com.bkrc.bkrcv3.gpt.client;

import com.bkrc.bkrcv3.ai.Ai;
import com.bkrc.bkrcv3.gpt.client.request.GptMessage;
import com.bkrc.bkrcv3.gpt.client.request.GptRequest;
import com.bkrc.bkrcv3.gpt.client.response.GptResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class Gpt implements Ai {
    private final GptClient gptClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getRecommend(String bookTitle) {
        var msgs = List.of(
                new GptMessage("system", "너는 누군가의 작은 불씨를 살리는 책을 추천하는 역할이야. 그리고 책속의 3가지 이하의 명언이나 좋은 내용의 문장을 3가지 이하 소개하는 책임이 있어"),
                new GptMessage("user", "참을 수 없는 존재의 가벼움"),
                new GptMessage("assistant", "{'result':['사랑은 힘을 빼는 것이다.','한 번은 아무것도 아니다. 한 번뿐인 것은 전혀 있었던 것이 아니다.']}"),
                new GptMessage("user", "노인과 바다"),
                new GptMessage("assistant", "{'result':['인간은 패배하도록 만들어지지 않았다. 인간은 파괴될 수는 있어도 패배하지는 않는다.','지금은 생각할 때가 아니다. 지금은 해야 할 때다']}"),
                new GptMessage("user", bookTitle)
        );
        GptRequest gptRequest = GptRequest.builder()
                .messages(msgs).build();
        GptResponse gptResponse = gptClient.getChatResponse(gptRequest);
        var choices = gptResponse.getChoices();
        if (CollectionUtils.isEmpty(gptResponse.getChoices())) return List.of();
        var choiceMsg = choices.get(0).getMessage();
        if (choiceMsg == null) return List.of();
        String content = choiceMsg.content();
        Map<String, List<String>> result = new HashMap<>();
        try {
            result = objectMapper.readValue(
                    content,
                    new TypeReference<Map<String, List<String>>>() {}
            );
            return result.get("result");
        } catch (Exception e) {
            log.info("gpt msg convert error : {}", e.getMessage());
        }

        return List.of();
    }
}
