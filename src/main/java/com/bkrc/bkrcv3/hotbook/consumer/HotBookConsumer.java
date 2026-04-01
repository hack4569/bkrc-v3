package com.bkrc.bkrcv3.hotbook.consumer;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.hotbook.application.HotBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotBookConsumer {
    private final HotBookService hotBookService;

    @KafkaListener( topics = {
            EventType.Topic.BOOK_LIKE
    })
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            hotBookService.handleEvent(event);
        }
    }
}
