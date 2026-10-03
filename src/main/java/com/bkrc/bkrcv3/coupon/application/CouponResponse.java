package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.coupon.entity.Coupon;
import com.bkrc.bkrcv3.coupon.entity.MemberCoupon;

import java.time.LocalDateTime;

/** 사용자에게 노출할 쿠폰 정책 응답입니다. */
public record CouponResponse(Long couponId, String name, LocalDateTime validFrom, LocalDateTime validUntil,
                             LocalDateTime publishFrom, LocalDateTime publishUntil, int remainingStock) {
    static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getCouponId(), coupon.getName(), coupon.getValidFrom(), coupon.getValidUntil(),
                coupon.getPublishFrom(), coupon.getPublishUntil(), coupon.getStock() - coupon.getIssuedCount());
    }

    /** 회원에게 실제 발급된 쿠폰의 발급 및 사용 상태 응답입니다. */
    public record Issued(Long memberCouponId, Long couponId, String name, LocalDateTime validFrom,
                         LocalDateTime validUntil, LocalDateTime issuedAt, LocalDateTime usedAt) {
        static Issued from(MemberCoupon value, Coupon coupon) {
            return new Issued(value.getMemberCouponId(), coupon.getCouponId(), coupon.getName(), coupon.getValidFrom(),
                    coupon.getValidUntil(), value.getIssuedAt(), value.getUsedAt());
        }
    }
}
