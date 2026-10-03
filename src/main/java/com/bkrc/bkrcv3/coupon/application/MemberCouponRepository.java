package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    /** 서비스 수준에서 중복 발급을 빠르게 판별합니다. 최종 방어선은 DB 유니크 제약입니다. */
    boolean existsByCouponIdAndMemberId(Long couponId, Long memberId);

    /** 회원이 발급받은 쿠폰을 최근 발급 순으로 반환합니다. */
    List<MemberCoupon> findByMemberIdOrderByIssuedAtDesc(Long memberId);

    /** 다른 회원의 쿠폰을 사용할 수 없도록 발급 ID와 회원 ID를 함께 조회 조건으로 사용합니다. */
    Optional<MemberCoupon> findByMemberCouponIdAndMemberId(Long memberCouponId, Long memberId);
}
