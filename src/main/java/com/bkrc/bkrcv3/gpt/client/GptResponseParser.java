package com.bkrc.bkrcv3.gpt.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GptResponseParser {

    private final ObjectMapper objectMapper;

    public List<String> parseStringList(String content) {
        return parse(content, new TypeReference<Map<String, List<String>>>() {})
                .map(m -> m.get(GptConstants.CHAT_RESPONSE_KEY))
                .orElse(List.of());
    }

    public String parseString(String content) {
        return parse(content, new TypeReference<Map<String, String>>() {})
                .map(m -> m.get(GptConstants.CHAT_RESPONSE_KEY))
                .orElse("");
    }

    private <T> Optional<T> parse(String content, TypeReference<T> typeRef) {
        try {
            return Optional.ofNullable(objectMapper.readValue(content, typeRef));
        } catch (Exception e) {
            log.warn("GPT 응답 파싱 실패 - content: {}, error: {}", content, e.getMessage());
            return Optional.empty();
        }
    }
}
