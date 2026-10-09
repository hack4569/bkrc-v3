package com.bkrc.bkrcv3.coupon.application.provided;

import com.bkrc.bkrcv3.api.CommonApiTest;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.coupon.application.CouponRepository;
import com.bkrc.bkrcv3.coupon.application.MemberCouponRepository;
import com.bkrc.bkrcv3.coupon.domain.Coupon;
import com.bkrc.bkrcv3.coupon.domain.CouponException;
import com.bkrc.bkrcv3.coupon.domain.MemberCoupon;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@CommonApiTest
@Transactional
class CouponRegisterTest {
    @Autowired CouponRegister couponRegister;
    @Autowired CouponRepository couponRepository;
    @Autowired MemberCouponRepository memberCouponRepository;
    @Autowired EntityManager entityManager;

    private Long memberCouponId;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = couponRepository.save(Coupon.create("테스트 쿠폰", now.minusDays(1), now.plusDays(1),
                now.minusDays(1), now.plusDays(1), 100));
        memberCouponId = 2001L;
        memberCouponRepository.save(MemberCoupon.issue(memberCouponId, coupon.getCouponId(), 10L, now));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void useOwnedCoupon() {
        var result = couponRegister.use(memberCouponId, 10L);

        assertThat(result.memberCouponId()).isEqualTo(memberCouponId);
        assertThat(result.usedAt()).isNotNull();
    }

    @Test
    void failWhenCouponBelongsToAnotherMember() {
        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> couponRegister.use(memberCouponId, 20L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_COUPON_NOT_FOUND);
    }

    @Test
    void failWhenCouponIsUsedTwice() {
        couponRegister.use(memberCouponId, 10L);

        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> couponRegister.use(memberCouponId, 10L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUPON_ALREADY_USED);
    }
}
