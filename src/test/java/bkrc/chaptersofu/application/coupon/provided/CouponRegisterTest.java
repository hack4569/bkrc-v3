package bkrc.chaptersofu.application.coupon.provided;

import bkrc.chaptersofu.application.coupon.provided.*;

import bkrc.chaptersofu.adapter.webapi.CommonApiTest;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.application.coupon.CouponRepository;
import bkrc.chaptersofu.application.coupon.MemberCouponRepository;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.CouponException;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
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
    void 소유한_쿠폰을_사용한다() {
        var result = couponRegister.use(memberCouponId, 10L);

        assertThat(result.getMemberCouponId()).isEqualTo(memberCouponId);
        assertThat(result.getUsedAt()).isNotNull();
    }

    @Test
    void 다른_회원의_쿠폰은_사용할_수_없다() {
        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> couponRegister.use(memberCouponId, 20L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_COUPON_NOT_FOUND);
    }

    @Test
    void 사용한_쿠폰은_다시_사용할_수_없다() {
        couponRegister.use(memberCouponId, 10L);

        CouponException exception = catchThrowableOfType(CouponException.class,
                () -> couponRegister.use(memberCouponId, 10L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUPON_ALREADY_USED);
    }
}
