package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    /** 중복 이벤트가 동일 발급의 재전송인지, 다른 ID로 요청된 중복 발급인지 판별합니다. */
    Optional<MemberCoupon> findByCouponIdAndMemberId(Long couponId, Long memberId);

    /** Redis가 비어 있을 때 실제 발급 이력으로 재고 카운터를 복구합니다. */
    long countByCouponId(Long couponId);

    /** 회원이 발급받은 쿠폰을 최근 발급 순으로 반환합니다. */
    List<MemberCoupon> findByMemberIdOrderByIssuedAtDesc(Long memberId);

    /** 다른 회원의 쿠폰을 사용할 수 없도록 발급 ID와 회원 ID를 함께 조회 조건으로 사용합니다. */
    Optional<MemberCoupon> findByMemberCouponIdAndMemberId(Long memberCouponId, Long memberId);

    /** RabbitMQ의 at-least-once 전달에서 중복 이벤트를 오류 없이 무시합니다. */
    @Modifying
    @Query(value = "insert into member_coupon " +
            "(member_coupon_id, coupon_id, member_id, issued_at) " +
            "values (:memberCouponId, :couponId, :memberId, :issuedAt) " +
            "on duplicate key update member_coupon_id = member_coupon_id", nativeQuery = true)
    int insertIfAbsent(@Param("memberCouponId") Long memberCouponId,
                       @Param("couponId") Long couponId,
                       @Param("memberId") Long memberId,
                       @Param("issuedAt") LocalDateTime issuedAt);
}
