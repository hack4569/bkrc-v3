package bkrc.chaptersofu.application.coupon.provided;

import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;

import java.util.List;

public interface CouponFinder {
    Coupon getCoupon(Long couponId);
    List<Coupon> getDownloadable();
    List<MemberCoupon> getMine(Long memberId);
}
