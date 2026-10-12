package bkrc.chaptersofu.domain.coupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CouponTest {
    private final LocalDateTime 기준시각 = LocalDateTime.of(2026, 10, 12, 12, 0);

    @Test
    void 쿠폰을_생성하면_활성화되고_발급수는_0이다() {
        Coupon coupon = 쿠폰(기준시각.minusDays(1), 기준시각.plusDays(1));

        assertThat(coupon.isActive()).isTrue();
        assertThat(coupon.getIssuedCount()).isZero();
        assertThat(coupon.getStock()).isEqualTo(100);
    }

    @Test
    void 게시기간과_유효기간_안이면_다운로드할_수_있다() {
        Coupon coupon = 쿠폰(기준시각.minusDays(1), 기준시각.plusDays(1));

        assertThat(coupon.isDownloadableAt(기준시각)).isTrue();
    }

    @Test
    void 유효기간_경계시각도_다운로드할_수_있다() {
        LocalDateTime 시작 = 기준시각.minusDays(1);
        LocalDateTime 종료 = 기준시각.plusDays(1);
        Coupon coupon = 쿠폰(시작, 종료);

        assertThat(coupon.isDownloadableAt(시작)).isTrue();
        assertThat(coupon.isDownloadableAt(종료)).isTrue();
    }

    @Test
    void 유효기간_밖이면_다운로드할_수_없다() {
        LocalDateTime 시작 = 기준시각.minusDays(1);
        LocalDateTime 종료 = 기준시각.plusDays(1);
        Coupon coupon = 쿠폰(시작, 종료);

        assertThat(coupon.isDownloadableAt(시작.minusNanos(1))).isFalse();
        assertThat(coupon.isDownloadableAt(종료.plusNanos(1))).isFalse();
    }

    private Coupon 쿠폰(LocalDateTime 시작, LocalDateTime 종료) {
        return Coupon.create("테스트 쿠폰", 시작, 종료, 시작, 종료, 100);
    }
}
