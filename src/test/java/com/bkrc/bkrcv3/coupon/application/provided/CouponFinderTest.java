package com.bkrc.bkrcv3.coupon.application.provided;

import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.coupon.application.CouponRepository;
import com.bkrc.bkrcv3.coupon.application.MemberCouponRepository;
import com.bkrc.bkrcv3.coupon.domain.Coupon;
import com.bkrc.bkrcv3.coupon.domain.MemberCoupon;
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
            assertThat(issued.memberCouponId()).isEqualTo(1001L);
            assertThat(issued.couponId()).isEqualTo(coupon.getCouponId());
            assertThat(issued.name()).isEqualTo("테스트 쿠폰");
            assertThat(issued.usedAt()).isNull();
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
