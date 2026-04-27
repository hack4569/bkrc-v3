package com.bkrc.bkrcv3.outbox;

import com.bkrc.bkrcv3.common.event.EventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private String exchange;
    private String routingKey;
    private String payload;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    // 정적 팩토리 메서드
    public static Outbox of(EventType eventType, String exchange, String routingKey, String payload) {
        Outbox outbox = new Outbox();
        outbox.eventType = eventType;
        outbox.exchange = exchange;
        outbox.routingKey = routingKey;
        outbox.payload = payload;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }

}