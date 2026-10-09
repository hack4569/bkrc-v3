package com.bkrc.bkrcv3.coupon.application.provided;

import com.bkrc.bkrcv3.coupon.application.CouponResponse;

public interface CouponRegister {
    CouponResponse.Issued download(Long couponId, Long memberId);
    CouponResponse.Issued use(Long memberCouponId, Long memberId);
}
