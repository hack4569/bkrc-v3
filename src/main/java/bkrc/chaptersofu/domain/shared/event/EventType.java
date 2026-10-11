package bkrc.chaptersofu.domain.shared.event;

import bkrc.chaptersofu.application.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.event.payload.CouponIssuedEventPayload;
import bkrc.chaptersofu.application.required.EventPayload;
import bkrc.chaptersofu.application.event.payload.MemberJoinEventPayload;
import bkrc.chaptersofu.application.event.payload.MemberModifyEventPayload;
import bkrc.chaptersofu.application.event.payload.MemberWithdrawEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    MEMBER_JOIN(MemberJoinEventPayload.class, Topic.MEMBER_JOIN),
    MEMBER_MODIFY(MemberModifyEventPayload.class, Topic.MEMBER_MODIFY),
    MEMBER_WITHDRAW(MemberWithdrawEventPayload.class, Topic.MEMBER_WITHDRAW),
    BOOK_LIKE(BookLikeEventPayload.class, Topic.BOOK_LIKE),
    COUPON_ISSUED(CouponIssuedEventPayload.class, Topic.COUPON_ISSUED);

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
        public static final String MEMBER_WITHDRAW = "member_withdraw";
        public static final String BOOK_LIKE = "book_like";
        public static final String BOOK_CLICK = "book_click";
        public static final String COUPON_ISSUED = "coupon_issued";
    }
}
