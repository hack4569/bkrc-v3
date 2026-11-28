package bkrc.chaptersofu.adapter.messaging.consumer;

import bkrc.chaptersofu.application.coupon.CouponRedisRepository;
import bkrc.chaptersofu.application.coupon.MemberCouponRepository;
import bkrc.chaptersofu.application.coupon.event.payload.CouponIssuedEventPayload;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RabbitMQ 재전송(at-least-once) 상황에서 쿠폰 발급 이벤트가 멱등하게 처리되는지 검증한다.
 */
class CouponIssueConsumerTest {
    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 11, 28, 12, 0);

    private MemberCouponRepository memberCouponRepository;
    private CouponRedisRepository couponRedisRepository;
    private CouponIssueConsumer consumer;

    @BeforeEach
    void setUp() {
        memberCouponRepository = mock(MemberCouponRepository.class);
        couponRedisRepository = mock(CouponRedisRepository.class);
        consumer = new CouponIssueConsumer(memberCouponRepository, couponRedisRepository);
    }

    @Test
    void 신규_발급_이벤트는_회원_쿠폰을_저장한다() {
        when(memberCouponRepository.insertIfAbsent(100L, 1L, 10L, ISSUED_AT)).thenReturn(1);

        consumer.listen(message(100L));

        verify(memberCouponRepository).insertIfAbsent(100L, 1L, 10L, ISSUED_AT);
        verifyNoInteractions(couponRedisRepository);
    }

    @Test
    void 같은_이벤트가_재전송되면_보상하지_않고_무시한다() {
        when(memberCouponRepository.insertIfAbsent(any(), any(), any(), any())).thenReturn(0);
        when(memberCouponRepository.findByCouponIdAndMemberId(1L, 10L))
                .thenReturn(Optional.of(MemberCoupon.issue(100L, 1L, 10L, ISSUED_AT)));

        consumer.listen(message(100L));

        verifyNoInteractions(couponRedisRepository);
    }

    @Test
    void 다른_ID의_중복_발급이면_Redis_카운터를_보상한다() {
        when(memberCouponRepository.insertIfAbsent(any(), any(), any(), any())).thenReturn(0);
        when(memberCouponRepository.findByCouponIdAndMemberId(1L, 10L))
                .thenReturn(Optional.of(MemberCoupon.issue(99L, 1L, 10L, ISSUED_AT)));

        consumer.listen(message(100L));

        verify(couponRedisRepository).compensateDuplicate(1L, 10L, 100L);
    }

    @Test
    void 중복인데_기존_발급_내역이_없으면_예외가_발생한다() {
        when(memberCouponRepository.insertIfAbsent(any(), any(), any(), any())).thenReturn(0);
        when(memberCouponRepository.findByCouponIdAndMemberId(1L, 10L)).thenReturn(Optional.empty());

        // 예외를 던져야 메시지가 DLQ로 이동해 데이터 불일치를 추적할 수 있다.
        assertThatThrownBy(() -> consumer.listen(message(100L)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 역직렬화할_수_없는_메시지는_거부한다() {
        assertThatThrownBy(() -> consumer.listen("not-json"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private String message(Long memberCouponId) {
        return Event.of(EventType.COUPON_ISSUED,
                new CouponIssuedEventPayload(memberCouponId, 1L, 10L, ISSUED_AT)).toJson();
    }
}
