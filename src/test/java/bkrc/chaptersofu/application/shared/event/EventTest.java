package bkrc.chaptersofu.application.shared.event;

import bkrc.chaptersofu.application.coupon.event.payload.CouponIssuedEventPayload;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
import bkrc.chaptersofu.application.required.event.EventType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {
    @Test
    void JSON에서_타입과_payload를_복원한다() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 10, 11, 12, 0);
        Event<EventPayload> source = Event.of(EventType.COUPON_ISSUED,
                new CouponIssuedEventPayload(1L, 2L, 3L, issuedAt));
        String json = source.toJson();

        assertThat(Event.typeFromJson(json)).isEqualTo(EventType.COUPON_ISSUED);
        Event<EventPayload> restored = Event.fromJson(json, CouponIssuedEventPayload.class);
        assertThat(restored.getType()).isEqualTo(EventType.COUPON_ISSUED);
        assertThat(restored.getPayload()).isInstanceOfSatisfying(CouponIssuedEventPayload.class, payload -> {
            assertThat(payload.getMemberCouponId()).isEqualTo(1L);
            assertThat(payload.getCouponId()).isEqualTo(2L);
            assertThat(payload.getMemberId()).isEqualTo(3L);
            assertThat(payload.getIssuedAt()).isEqualTo(issuedAt);
        });
    }
}
