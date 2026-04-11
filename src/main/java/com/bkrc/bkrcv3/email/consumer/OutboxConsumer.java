package com.bkrc.bkrcv3.email.consumer;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.required.EventPayload;
import com.bkrc.bkrcv3.email.application.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxConsumer {
    private final EmailService emailService;

    @KafkaListener( topics = {
            EventType.Topic.MEMBER_JOIN,
            EventType.Topic.MEMBER_MODIFY
    })
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            emailService.handleEvent(event);
        }
    }
}
