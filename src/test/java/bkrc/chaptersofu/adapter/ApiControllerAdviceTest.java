package bkrc.chaptersofu.adapter;

import bkrc.chaptersofu.domain.aladin.AladinClientException;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.application.aladin.provided.AladinFinder;
import bkrc.chaptersofu.application.hotbook.HotBookCalculator;
import bkrc.chaptersofu.application.hotbook.HotBookRepository;
import bkrc.chaptersofu.application.hotbook.HotBookService;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.EventHandlerException;
import bkrc.chaptersofu.domain.member.DuplicateMemberException;
import bkrc.chaptersofu.domain.member.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class ApiControllerAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BusinessExceptionTestController())
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
    }

    @Test
    void MemberNotFoundException_구현체_클래스명을_로그에_출력한다(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/test/business-exception/member-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.exception").value(MemberNotFoundException.class.getSimpleName()));

        assertThat(output.getOut())
                .contains("MemberNotFoundException")
                .contains("errorCode=EU3")
                .contains("status=404")
                .contains("method=GET")
                .contains("uri=/test/business-exception/member-not-found");
    }

    @Test
    void DuplicateMemberException_구현체_클래스명을_로그에_출력한다(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/test/business-exception/duplicate-member"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.USER_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.exception").value(DuplicateMemberException.class.getSimpleName()));

        assertThat(output.getOut())
                .contains("DuplicateMemberException")
                .contains("errorCode=EU2")
                .contains("status=409")
                .contains("method=GET")
                .contains("uri=/test/business-exception/duplicate-member");
    }

    @Test
    void HotBookService에서_발생한_EventHandlerException을_전역_예외처리기가_로그에_출력한다(
            CapturedOutput output
    ) throws Exception {
        String endpoint = "/test/business-exception/event-handler-not-found";

        mockMvc.perform(get(endpoint))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.EVENT_HANDLER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.exception").value(EventHandlerException.class.getSimpleName()));

        assertThat(output.getOut())
                .contains("EventHandlerException")
                .contains("errorCode=ECM2")
                .contains("status=500")
                .contains("method=GET")
                .contains("uri=" + endpoint)
                .contains("message=" + ErrorCode.EVENT_HANDLER_NOT_FOUND.getMessage())
                .contains("eventType=BOOK_LIKE");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("aladinBusinessExceptionCases")
    void Aladin_도메인_예외의_구현체와_오류정보를_로그에_출력한다(
            String description,
            String endpoint,
            Class<? extends RuntimeException> exceptionType,
            ErrorCode errorCode,
            CapturedOutput output
    ) throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().is(errorCode.getStatus().value()))
                .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
                .andExpect(jsonPath("$.exception").value(exceptionType.getSimpleName()));

        assertThat(output.getOut())
                .contains(exceptionType.getSimpleName())
                .contains("errorCode=" + errorCode.getCode())
                .contains("status=" + errorCode.getStatus().value())
                .contains("method=GET")
                .contains("uri=" + endpoint)
                .contains("message=" + errorCode.getMessage());
    }

    static Stream<Arguments> aladinBusinessExceptionCases() {
        return Stream.of(
                Arguments.of(
                        "알라딘 응답이 준비되지 않은 경우",
                        "/test/business-exception/aladin-not-ready",
                        AladinException.class,
                        ErrorCode.ALADIN_NOT_READY
                ),
                Arguments.of(
                        "알라딘 API 호출에 실패한 경우",
                        "/test/business-exception/aladin-client-error",
                        AladinClientException.class,
                        ErrorCode.ALADIN_CLIENT_ERROR
                ),
                Arguments.of(
                        "알라딘 책 정보를 찾지 못한 경우",
                        "/test/business-exception/aladin-not-found",
                        AladinException.class,
                        ErrorCode.ALADIN_NOT_FOUND
                ),
                Arguments.of(
                        "도서 상품번호를 찾지 못한 경우",
                        "/test/business-exception/book-not-found",
                        AladinException.class,
                        ErrorCode.BOOK_NOT_FOUND
                )
        );
    }

    @RestController
    static class BusinessExceptionTestController {

        @GetMapping("/test/business-exception/member-not-found")
        void memberNotFound() {
            throw new MemberNotFoundException(10L);
        }

        @GetMapping("/test/business-exception/duplicate-member")
        void duplicateMember() {
            throw new DuplicateMemberException(ErrorCode.USER_ALREADY_EXISTS);
        }

        @GetMapping("/test/business-exception/event-handler-not-found")
        void eventHandlerNotFound() {
            HotBookService hotBookService = new HotBookService(
                    java.util.List.of(),
                    mock(HotBookCalculator.class),
                    mock(HotBookRepository.class),
                    mock(AladinFinder.class)
            );
            Event<EventPayload> event = Event.of(EventType.BOOK_LIKE, new TestEventPayload(10));

            hotBookService.handleEvent(event);
        }

        @GetMapping("/test/business-exception/aladin-not-ready")
        void aladinNotReady() {
            throw new AladinException(ErrorCode.ALADIN_NOT_READY);
        }

        @GetMapping("/test/business-exception/aladin-client-error")
        void aladinClientError() {
            throw new AladinClientException(new IllegalStateException("외부 API 연결 실패"));
        }

        @GetMapping("/test/business-exception/aladin-not-found")
        void aladinNotFound() {
            throw new AladinException(ErrorCode.ALADIN_NOT_FOUND);
        }

        @GetMapping("/test/business-exception/book-not-found")
        void bookNotFound() {
            throw new AladinException(ErrorCode.BOOK_NOT_FOUND);
        }
    }

    private record TestEventPayload(Integer bookId) implements EventPayload {
    }
}
