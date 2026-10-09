package com.bkrc.bkrcv3.coupon.application.provided;

import com.bkrc.bkrcv3.coupon.application.CouponResponse;

import java.util.List;

public interface CouponFinder {
    List<CouponResponse> getDownloadable();
    List<CouponResponse.Issued> getMine(Long memberId);
}
