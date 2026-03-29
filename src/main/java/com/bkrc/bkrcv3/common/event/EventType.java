package com.bkrc.bkrcv3.common.event;

import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.common.event.payload.MemberJoinEventPayload;
import com.bkrc.bkrcv3.common.event.payload.MemberModifyEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    MEMBER_JOIN(MemberJoinEventPayload.class,
            Topic.MEMBER_JOIN,
            Queue.MEMBER_JOIN),
    MEMBER_MODIFY(MemberModifyEventPayload.class,
            Topic.MEMBER_MODIFY,
            Queue.MEMBER_MODIFY);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;
    private final String routingKey; // RabbitMQ routing key

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    // Kafka topic (기존 유지)
    public static class Topic {
        public static final String MEMBER_JOIN   = "member_join";
        public static final String MEMBER_MODIFY = "member_modify";
    }

    // RabbitMQ Queue 이름
    public static class Queue {
        public static final String MEMBER_JOIN   = "member.join";
        public static final String MEMBER_MODIFY = "member.modify";
    }

    // RabbitMQ Exchange 이름
    public static final String EXCHANGE = "member.exchange";
}