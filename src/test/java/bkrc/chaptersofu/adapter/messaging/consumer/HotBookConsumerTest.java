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
                BookLikeEventPayload.builder().bookId(10).memberId(20L).delta(1).build());

        consumer.listen(event.toJson());

        verify(service).handleEvent(argThat(received ->
                received.getType() == EventType.BOOK_LIKE
                        && received.getPayload() instanceof BookLikeEventPayload payload
                        && payload.getBookId().equals(10)
                        && payload.getDelta().equals(1)));
    }
}
