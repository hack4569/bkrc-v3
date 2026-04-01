package com.bkrc.bkrcv3.common.event;

import com.bkrc.bkrcv3.common.event.payload.BookLikeEventPayload;
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
    MEMBER_JOIN(MemberJoinEventPayload.class, Topic.MEMBER_JOIN),
    MEMBER_MODIFY(MemberModifyEventPayload.class, Topic.MEMBER_MODIFY),
    BOOK_LIKE(BookLikeEventPayload.class, Topic.BOOK_LIKE);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String MEMBER_JOIN = "member_join";
        public static final String MEMBER_MODIFY = "member_modify";
        public static final String BOOK_LIKE = "book_like";
        public static final String BOOK_CLICK = "book_click";
    }
}
