package bkrc.chaptersofu.application.required.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public enum EventType {
    MEMBER_JOIN(Topic.MEMBER_JOIN),
    MEMBER_MODIFY(Topic.MEMBER_MODIFY),
    MEMBER_WITHDRAW(Topic.MEMBER_WITHDRAW),
    BOOK_LIKE(Topic.BOOK_LIKE),
    COUPON_ISSUED(Topic.COUPON_ISSUED);

    private final String topic;

    public String getTopic() {
        return topic;
    }

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
        public static final String MEMBER_WITHDRAW = "member_withdraw";
        public static final String BOOK_LIKE = "book_like";
        public static final String BOOK_CLICK = "book_click";
        public static final String COUPON_ISSUED = "coupon_issued";
    }
}
