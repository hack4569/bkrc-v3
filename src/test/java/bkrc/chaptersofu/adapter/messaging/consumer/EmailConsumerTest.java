package bkrc.chaptersofu.adapter.messaging.consumer;

import bkrc.chaptersofu.application.email.EmailService;
import bkrc.chaptersofu.application.member.event.payload.MemberJoinEventPayload;
import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventPayload;
import bkrc.chaptersofu.application.shared.event.EventType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class EmailConsumerTest {
    private final EmailService emailService = mock(EmailService.class);
    private final EmailConsumer consumer = new EmailConsumer(emailService);

    @Test
    void 회원가입_메시지를_역직렬화해_이메일_서비스에_전달한다() {
        Event<EventPayload> event = Event.of(EventType.MEMBER_JOIN,
                new MemberJoinEventPayload(1L, "member", LocalDateTime.now()));

        consumer.listen(event.toJson());

        verify(emailService).handleEvent(argThat(received ->
                received.getType() == EventType.MEMBER_JOIN
                        && received.getPayload() instanceof MemberJoinEventPayload payload
                        && payload.getMemberId().equals(1L)));
    }

    @Test
    void 이메일과_관계없는_이벤트는_무시한다() {
        Event<EventPayload> event = Event.of(EventType.BOOK_LIKE,
                BookLikeEventPayload.builder().bookId(10).memberId(20L).delta(1).build());

        consumer.listen(event.toJson());

        verifyNoInteractions(emailService);
    }
}
