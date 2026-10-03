package com.bkrc.bkrcv3.coupon.application;

import com.bkrc.bkrcv3.coupon.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    /** 현재 시각에 노출 및 다운로드 가능한 쿠폰만 조회합니다. */
    @Query("select c from Coupon c where c.active = true and c.publishFrom <= :now and c.publishUntil >= :now " +
            "and c.validFrom <= :now and c.validUntil >= :now and c.issuedCount < c.stock order by c.publishFrom desc")
    List<Coupon> findDownloadable(@Param("now") LocalDateTime now);

    /**
     * 발급 트랜잭션 동안 쿠폰 행을 잠가 동시 요청이 설정 재고를 초과하지 못하게 합니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.couponId = :id")
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);
}
