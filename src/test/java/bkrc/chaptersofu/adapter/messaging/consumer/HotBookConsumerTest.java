package bkrc.chaptersofu.adapter.messaging.consumer;

import bkrc.chaptersofu.application.hotbook.HotBookService;
import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
import bkrc.chaptersofu.application.required.event.EventType;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class HotBookConsumerTest {
    @Test
    void 좋아요_메시지를_역직렬화해_핫북_서비스에_전달한다() {
        HotBookService service = mock(HotBookService.class);
        HotBookConsumer consumer = new HotBookConsumer(service);
        Event<EventPayload> event = Event.of(EventType.BOOK_LIKE,
                BookLikeEventPayload.builder().bookId(10L).memberId(20L).delta(1).build());

        consumer.listen(event.toJson());

        verify(service).handleEvent(argThat(received ->
                received.getType() == EventType.BOOK_LIKE
                        && received.getPayload() instanceof BookLikeEventPayload payload
                        // bookId는 Long 타입이므로 Integer 리터럴(10)과 equals 비교하면 항상 false가 된다.
                        && payload.getBookId().equals(10L)
                        && payload.getDelta().equals(1)));
    }
}
