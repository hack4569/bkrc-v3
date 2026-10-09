package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.coupon.domain.Coupon;
import com.bkrc.bkrcv3.coupon.domain.MemberCoupon;

import java.time.LocalDateTime;

/** 사용자에게 노출할 쿠폰 정책 응답입니다. */
public record CouponResponse(Long couponId, String name, LocalDateTime validFrom, LocalDateTime validUntil,
                             LocalDateTime publishFrom, LocalDateTime publishUntil, int remainingStock) {
    static CouponResponse from(Coupon coupon, int remainingStock) {
        return new CouponResponse(coupon.getCouponId(), coupon.getName(), coupon.getValidFrom(), coupon.getValidUntil(),
                coupon.getPublishFrom(), coupon.getPublishUntil(), remainingStock);
    }

    /** 회원에게 실제 발급된 쿠폰의 발급 및 사용 상태 응답입니다. */
    public record Issued(Long memberCouponId, Long couponId, String name, LocalDateTime validFrom,
                         LocalDateTime validUntil, LocalDateTime issuedAt, LocalDateTime usedAt,
                         Integer remainingStock) {
        static Issued from(MemberCoupon value, Coupon coupon) {
            return new Issued(value.getMemberCouponId(), coupon.getCouponId(), coupon.getName(), coupon.getValidFrom(),
                    coupon.getValidUntil(), value.getIssuedAt(), value.getUsedAt(), null);
        }

        static Issued pending(Long memberCouponId, Coupon coupon, LocalDateTime issuedAt, int remainingStock) {
            return new Issued(memberCouponId, coupon.getCouponId(), coupon.getName(), coupon.getValidFrom(),
                    coupon.getValidUntil(), issuedAt, null, remainingStock);
        }
    }
}
