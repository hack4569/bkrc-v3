package bkrc.chaptersofu.adapter.webapi.dto;

import bkrc.chaptersofu.domain.coupon.Coupon;
import bkrc.chaptersofu.domain.coupon.MemberCoupon;

import java.time.LocalDateTime;

public record CouponResponse(Long couponId, String name, LocalDateTime validFrom, LocalDateTime validUntil,
                             LocalDateTime publishFrom, LocalDateTime publishUntil, int remainingStock) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getCouponId(), coupon.getName(), coupon.getValidFrom(), coupon.getValidUntil(),
                coupon.getPublishFrom(), coupon.getPublishUntil(), coupon.getStock() - coupon.getIssuedCount());
    }

    public record Issued(Long memberCouponId, Long couponId, String name, LocalDateTime validFrom,
                         LocalDateTime validUntil, LocalDateTime issuedAt, LocalDateTime usedAt,
                         Integer remainingStock) {
        public static Issued from(MemberCoupon memberCoupon, Coupon coupon) {
            return new Issued(memberCoupon.getMemberCouponId(), coupon.getCouponId(), coupon.getName(),
                    coupon.getValidFrom(), coupon.getValidUntil(), memberCoupon.getIssuedAt(),
                    memberCoupon.getUsedAt(), null);
        }
    }
}
