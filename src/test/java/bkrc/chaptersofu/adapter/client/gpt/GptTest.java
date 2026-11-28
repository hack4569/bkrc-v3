package bkrc.chaptersofu.adapter.client.gpt;

import bkrc.chaptersofu.adapter.client.gpt.dto.GptChoice;
import bkrc.chaptersofu.adapter.client.gpt.dto.GptMessage;
import bkrc.chaptersofu.adapter.client.gpt.dto.GptRequest;
import bkrc.chaptersofu.adapter.client.gpt.dto.GptResponse;
import bkrc.chaptersofu.application.required.BookDescriptionSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * GPT 응답 본문을 도메인 값으로 변환하는 과정을 검증한다.
 * HTTP 호출은 GptClient 목으로 대체하고, 파서는 실제 구현을 사용해 JSON 처리까지 함께 확인한다.
 * Retry/CircuitBreaker는 스프링 프록시에서만 동작하므로 이 테스트의 범위가 아니다.
 */
class GptTest {
    private GptClient gptClient;
    private Gpt gpt;

    @BeforeEach
    void setUp() {
        gptClient = mock(GptClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        gpt = new Gpt(gptClient, new GptResponseParser(objectMapper), objectMapper);
    }

    @Test
    void 추천_문장_목록을_파싱하고_마지막_메시지로_도서명을_전달한다() {
        givenContent("{\"result\":[\"문장1\",\"문장2\"]}");

        assertThat(gpt.getRecommend("데미안")).containsExactly("문장1", "문장2");

        ArgumentCaptor<GptRequest> captor = ArgumentCaptor.forClass(GptRequest.class);
        verify(gptClient).getChatResponse(captor.capture());
        List<GptMessage> messages = captor.getValue().getMessages();
        assertThat(messages.get(messages.size() - 1)).isEqualTo(new GptMessage("user", "데미안"));
    }

    @Test
    void 코드펜스로_감싼_요약_응답도_파싱한다() {
        // 모델이 지시와 달리 마크다운 코드블록으로 응답하는 경우를 허용해야 한다.
        givenContent("```json\n{\"result\":\"요약된 소개\"}\n```");

        assertThat(gpt.filteringContent("긴 책 소개")).isEqualTo("요약된 소개");
    }

    @Test
    void 책_소개를_개요와_인사이트로_나누어_파싱한다() {
        givenContent("{\"overview\":\"어떤 책\",\"insight\":\"얻는 것\"}");

        assertThat(gpt.summarizeDescriptions("책 소개"))
                .isEqualTo(new BookDescriptionSummary("어떤 책", "얻는 것"));
    }

    @Test
    void 파싱할_수_없는_응답은_빈_값으로_대체한다() {
        givenContent("JSON이 아닌 응답");

        assertThat(gpt.getRecommend("데미안")).isEmpty();
        assertThat(gpt.filteringContent("소개")).isEmpty();
        assertThat(gpt.summarizeDescriptions("소개")).isEqualTo(BookDescriptionSummary.empty());
    }

    @Test
    void 선택지가_없거나_메시지가_없으면_빈_값을_반환한다() {
        when(gptClient.getChatResponse(any())).thenReturn(new GptResponse("id", "chat", 0L, "model", List.of()));
        assertThat(gpt.getRecommend("데미안")).isEmpty();

        when(gptClient.getChatResponse(any())).thenReturn(response(new GptChoice(0, null, "stop")));
        assertThat(gpt.summarizeDescriptions("소개")).isEqualTo(BookDescriptionSummary.empty());
    }

    private void givenContent(String content) {
        when(gptClient.getChatResponse(any()))
                .thenReturn(response(new GptChoice(0, new GptMessage("assistant", content), "stop")));
    }

    private GptResponse response(GptChoice choice) {
        return new GptResponse("id", "chat.completion", 0L, "gpt-4o-mini", List.of(choice));
    }
}
