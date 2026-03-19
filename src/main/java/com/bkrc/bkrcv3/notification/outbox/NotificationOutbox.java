package com.bkrc.bkrcv3.notification.outbox;

import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;    // PENDING, PUBLISHED, FAILED
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private String payload;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private int retryCount;


    public enum OutboxStatus {
        PENDING, PUBLISHED, FAILED
    }

    // 정적 팩토리 메서드
    public static NotificationOutbox of(EventType eventType, String payload) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.status = OutboxStatus.PENDING;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
    }
}