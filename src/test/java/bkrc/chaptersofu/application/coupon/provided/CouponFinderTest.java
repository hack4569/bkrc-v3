package bkrc.chaptersofu.application.coupon.provided;

import bkrc.chaptersofu.application.coupon.provided.*;

import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.application.coupon.CouponRepository;
import bkrc.chaptersofu.application.coupon.MemberCouponRepository;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@CommonApiTest
@Transactional
class CouponFinderTest {
    @Autowired CouponFinder couponFinder;
    @Autowired CouponRepository couponRepository;
    @Autowired MemberCouponRepository memberCouponRepository;
    @Autowired EntityManager entityManager;

    @Test
    void 회원에게_발급된_쿠폰을_조회한다() {
        Coupon coupon = couponRepository.save(validCoupon());
        memberCouponRepository.save(MemberCoupon.issue(1001L, coupon.getCouponId(), 10L, LocalDateTime.now()));
        entityManager.flush();
        entityManager.clear();

        var result = couponFinder.getMine(10L);

        assertThat(result).singleElement().satisfies(issued -> {
            assertThat(issued.getMemberCouponId()).isEqualTo(1001L);
            assertThat(issued.getCouponId()).isEqualTo(coupon.getCouponId());
            assertThat(issued.getUsedAt()).isNull();
        });
    }

    @Test
    void 회원_쿠폰이_없으면_빈_목록을_반환한다() {
        assertThat(couponFinder.getMine(999L)).isEmpty();
    }

    private Coupon validCoupon() {
        LocalDateTime now = LocalDateTime.now();
        return Coupon.create("테스트 쿠폰", now.minusDays(1), now.plusDays(1),
                now.minusDays(1), now.plusDays(1), 100);
    }
}
