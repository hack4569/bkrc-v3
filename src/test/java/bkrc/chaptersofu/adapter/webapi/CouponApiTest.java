package bkrc.chaptersofu.adapter.webapi;

import bkrc.chaptersofu.adapter.webapi.dto.CouponResponse;
import bkrc.chaptersofu.application.coupon.provided.CouponFinder;
import bkrc.chaptersofu.application.coupon.provided.CouponRegister;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 쿠폰 API가 서비스 결과를 쿠폰 원본 정보와 조합해 응답하는지 검증한다.
 * 인증/직렬화는 다른 통합 테스트에서 다루므로 컨트롤러 메서드를 직접 호출한다.
 */
class CouponApiTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 28, 12, 0);

    private final CouponFinder couponFinder = mock(CouponFinder.class);
    private final CouponRegister couponRegister = mock(CouponRegister.class);
    private final CouponApi api = new CouponApi(couponFinder, couponRegister);

    @Test
    void 쿠폰_목록과_발급_사용_결과를_응답으로_변환한다() {
        Coupon coupon = Coupon.create("쿠폰", NOW, NOW.plusDays(1), NOW, NOW.plusDays(1), 10);
        ReflectionTestUtils.setField(coupon, "couponId", 1L);
        MemberCoupon memberCoupon = MemberCoupon.issue(100L, 1L, 10L, NOW);
        when(couponFinder.getDownloadable()).thenReturn(List.of(coupon));
        when(couponFinder.getCoupon(1L)).thenReturn(coupon);
        when(couponFinder.getMine(10L)).thenReturn(List.of(memberCoupon));
        when(couponRegister.download(1L, 10L)).thenReturn(memberCoupon);
        when(couponRegister.use(100L, 10L)).thenReturn(memberCoupon);

        assertThat(api.coupons()).extracting(CouponResponse::couponId).containsExactly(1L);
        assertThat(api.download(1L, 10L).name()).isEqualTo("쿠폰");
        assertThat(api.mine(10L)).extracting(CouponResponse.Issued::memberCouponId).containsExactly(100L);
        assertThat(api.use(100L, 10L).couponId()).isEqualTo(1L);
    }
}
