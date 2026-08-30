package com.bkrc.bkrcv3.adapter.gpt;

import com.bkrc.bkrcv3.required.BookDescriptionSummary;
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

    public BookDescriptionSummary parseBookDescriptionSummary(String content) {
        return parse(content, new TypeReference<BookDescriptionSummary>() {})
                .orElseGet(BookDescriptionSummary::empty);
    }

    private <T> Optional<T> parse(String content, TypeReference<T> typeRef) {
        try {
            return Optional.ofNullable(objectMapper.readValue(stripMarkdownCodeFence(content), typeRef));
        } catch (Exception e) {
            log.warn("GPT 응답 파싱 실패 - content: {}, error: {}", content, e.getMessage());
            return Optional.empty();
        }
    }

    private String stripMarkdownCodeFence(String content) {
        if (content == null) {
            return null;
        }

        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineEnd = trimmed.indexOf('\n');
        int closingFence = trimmed.lastIndexOf("```");
        if (firstLineEnd < 0 || closingFence <= firstLineEnd) {
            return trimmed;
        }

        return trimmed.substring(firstLineEnd + 1, closingFence).trim();
    }
}
