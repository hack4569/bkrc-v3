package bkrc.chaptersofu.domain.coupon;

import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberCouponTest {
    private final LocalDateTime 기준시각 = LocalDateTime.of(2026, 10, 12, 12, 0);

    @Test
    void 회원에게_쿠폰을_발급한다() {
        MemberCoupon issued = MemberCoupon.issue(1L, 2L, 3L, 기준시각);

        assertThat(issued.getMemberCouponId()).isEqualTo(1L);
        assertThat(issued.getCouponId()).isEqualTo(2L);
        assertThat(issued.getMemberId()).isEqualTo(3L);
        assertThat(issued.getIssuedAt()).isEqualTo(기준시각);
        assertThat(issued.getUsedAt()).isNull();
    }

    @Test
    void 유효기간_안에_쿠폰을_사용한다() {
        MemberCoupon issued = MemberCoupon.issue(1L, 2L, 3L, 기준시각.minusDays(1));

        issued.use(기준시각, 유효한_쿠폰());

        assertThat(issued.getUsedAt()).isEqualTo(기준시각);
    }

    @Test
    void 이미_사용한_쿠폰은_재사용할_수_없다() {
        MemberCoupon issued = MemberCoupon.issue(1L, 2L, 3L, 기준시각.minusDays(1));
        issued.use(기준시각, 유효한_쿠폰());

        assertThatThrownBy(() -> issued.use(기준시각, 유효한_쿠폰()))
                .isInstanceOf(CouponException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPON_ALREADY_USED);
    }

    @Test
    void 유효기간이_지난_쿠폰은_사용할_수_없다() {
        MemberCoupon issued = MemberCoupon.issue(1L, 2L, 3L, 기준시각.minusDays(3));
        Coupon expired = Coupon.create("만료", 기준시각.minusDays(2), 기준시각.minusDays(1),
                기준시각.minusDays(3), 기준시각.minusDays(1), 1);

        assertThatThrownBy(() -> issued.use(기준시각, expired))
                .isInstanceOf(CouponException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPON_NOT_VALID);
    }

    private Coupon 유효한_쿠폰() {
        return Coupon.create("정상", 기준시각.minusDays(1), 기준시각.plusDays(1),
                기준시각.minusDays(1), 기준시각.plusDays(1), 1);
    }
}
