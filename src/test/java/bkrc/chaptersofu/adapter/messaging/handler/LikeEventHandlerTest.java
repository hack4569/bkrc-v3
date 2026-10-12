package bkrc.chaptersofu.adapter.messaging.handler;

import bkrc.chaptersofu.application.like.LikeCountEventProcessor;
import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.shared.event.EventPayload;
import bkrc.chaptersofu.application.shared.event.EventType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LikeEventHandlerTest {
    private final LikeCountEventProcessor processor = mock(LikeCountEventProcessor.class);
    private final LikeEventHandler handler = new LikeEventHandler(processor);

    @SuppressWarnings("unchecked")
    @Test
    void 좋아요_이벤트를_지원한다() {
        Event<BookLikeEventPayload> event = (Event<BookLikeEventPayload>) (Event<?>)
                Event.of(EventType.BOOK_LIKE, payload());

        assertThat(handler.supports(event)).isTrue();
        assertThat(handler.findBookId(event)).isEqualTo(10);
    }

    @SuppressWarnings("unchecked")
    @Test
    void 좋아요_이벤트를_집계_프로세서에_전달한다() {
        BookLikeEventPayload payload = payload();
        Event<BookLikeEventPayload> event = (Event<BookLikeEventPayload>) (Event<?>)
                Event.of(EventType.BOOK_LIKE, payload);

        handler.handle(event);

        verify(processor).process(eq(payload), any(Duration.class));
    }

    private BookLikeEventPayload payload() {
        return BookLikeEventPayload.builder().bookId(10).memberId(20L).delta(1).build();
    }
}
