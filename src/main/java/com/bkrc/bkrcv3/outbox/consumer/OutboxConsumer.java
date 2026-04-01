package com.bkrc.bkrcv3.outbox.consumer;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxConsumer {
    private final OutboxService outboxService;

    @KafkaListener( topics = {
            EventType.Topic.MEMBER_JOIN,
            EventType.Topic.MEMBER_MODIFY,
            EventType.Topic.BOOK_LIKE
    })
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            outboxService.handleEvent(event);
        }
    }
}
