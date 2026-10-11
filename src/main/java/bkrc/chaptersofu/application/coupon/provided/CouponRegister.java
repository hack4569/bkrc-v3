package bkrc.chaptersofu.application.coupon.provided;

import bkrc.chaptersofu.domain.coupon.MemberCoupon;

public interface CouponRegister {
    MemberCoupon download(Long couponId, Long memberId);
    MemberCoupon use(Long memberCouponId, Long memberId);
}
