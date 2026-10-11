package bkrc.chaptersofu.adapter.tests.coupon.application.provided;

import bkrc.chaptersofu.application.coupon.provided.*;

import bkrc.chaptersofu.adapter.tests.api.CommonApiTest;
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
    void findIssuedCouponsByMember() {
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
    void returnEmptyListWhenMemberHasNoCoupon() {
        assertThat(couponFinder.getMine(999L)).isEmpty();
    }

    private Coupon validCoupon() {
        LocalDateTime now = LocalDateTime.now();
        return Coupon.create("테스트 쿠폰", now.minusDays(1), now.plusDays(1),
                now.minusDays(1), now.plusDays(1), 100);
    }
}
