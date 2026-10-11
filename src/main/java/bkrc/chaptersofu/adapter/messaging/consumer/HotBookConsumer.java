package bkrc.chaptersofu.adapter.messaging.consumer;

import bkrc.chaptersofu.application.shared.event.Event;
import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.application.hotbook.HotBookService;
import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.shared.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotBookConsumer {
    private final HotBookService hotBookService;

    @RabbitListener(
            queues = {
                    RabbitMQConfig.LIKE_QUEUE
            }
    )
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message, BookLikeEventPayload.class);
        if (event != null) {
            hotBookService.handleEvent(event);
        }
    }
}
