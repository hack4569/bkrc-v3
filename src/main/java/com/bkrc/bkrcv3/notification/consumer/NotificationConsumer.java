package com.bkrc.bkrcv3.notification.consumer;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationService notificationService;

    @KafkaListener( topics = {
            EventType.Topic.MEMBER_JOIN,
            EventType.Topic.MEMBER_MODIFY
    })
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            notificationService.handleEvent(event);
        }
    }
}
