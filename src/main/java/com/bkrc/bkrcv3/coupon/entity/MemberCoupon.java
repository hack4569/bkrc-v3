package com.bkrc.bkrcv3.coupon.entity;

import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원에게 발급된 쿠폰과 사용 상태를 기록합니다.
 * 쿠폰-회원 복합 유니크 제약은 동일 회원의 중복 발급을 DB 수준에서 차단합니다.
 */
@Entity
@Table(name = "member_coupon", uniqueConstraints =
        @UniqueConstraint(name = "uk_member_coupon_coupon_member", columnNames = {"coupon_id", "member_id"}))
@Getter
public class MemberCoupon {
    @Id
    // 여러 API 인스턴스에서도 독립적으로 생성할 수 있도록 애플리케이션의 Snowflake ID를 사용합니다.
    @Column(name = "member_coupon_id", nullable = false, updatable = false)
    private Long memberCouponId;

    @Column(name = "coupon_id", nullable = false, updatable = false)
    private Long couponId;

    @Column(name = "member_id", nullable = false, updatable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    protected MemberCoupon() {}

    public static MemberCoupon issue(Long memberCouponId, Long couponId, Long memberId, LocalDateTime now) {
        MemberCoupon value = new MemberCoupon();
        value.memberCouponId = memberCouponId;
        value.couponId = couponId;
        value.memberId = memberId;
        value.issuedAt = now;
        return value;
    }

    public void use(LocalDateTime now, Coupon coupon) {
        // 사용 완료 여부를 먼저 확인해 같은 쿠폰의 재사용을 막습니다.
        if (usedAt != null) throw new BusinessException(ErrorCode.COUPON_ALREADY_USED);
        // 게시 기간과 관계없이 실제 사용 가능 여부는 쿠폰 유효기간으로 판단합니다.
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil()))
            throw new BusinessException(ErrorCode.COUPON_NOT_VALID);
        usedAt = now;
    }
}
