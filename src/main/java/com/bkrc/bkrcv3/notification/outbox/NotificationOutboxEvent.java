package com.bkrc.bkrcv3.notification.outbox;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NotificationOutboxEvent {
    private NotificationOutbox outbox;

    public static NotificationOutboxEvent of(NotificationOutbox outbox) {
        NotificationOutboxEvent outboxEvent = new NotificationOutboxEvent();
        outboxEvent.outbox = outbox;
        return outboxEvent;
    }
}
