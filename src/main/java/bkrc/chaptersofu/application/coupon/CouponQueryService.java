package bkrc.chaptersofu.application.coupon;

import bkrc.chaptersofu.application.coupon.provided.CouponFinder;
import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.CouponException;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponQueryService implements CouponFinder {
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRedisRepository couponRedisRepository;
    private final Clock couponClock;

    @Override
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));
    }

    @Override
    public List<Coupon> getDownloadable() {
        return couponRepository.findVisible(LocalDateTime.now(couponClock)).stream()
                .filter(coupon -> remainingStock(coupon) > 0)
                .toList();
    }

    @Override
    public List<MemberCoupon> getMine(Long memberId) {
        return memberCouponRepository.findByMemberIdOrderByIssuedAtDesc(memberId);
    }

    private int remainingStock(Coupon coupon) {
        return couponRedisRepository.remainingStock(coupon, initialIssuedCount(coupon.getCouponId()));
    }

    private long initialIssuedCount(Long couponId) {
        if (couponRedisRepository.isInitialized(couponId)) {
            return 0L;
        }
        return memberCouponRepository.countByCouponId(couponId);
    }
}
