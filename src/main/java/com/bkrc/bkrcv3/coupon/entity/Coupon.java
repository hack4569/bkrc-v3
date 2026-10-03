package com.bkrc.bkrcv3.coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자가 등록한 쿠폰 정책입니다.
 * 게시 기간은 사용자 화면 노출 기간이고, 유효기간은 다운로드 및 사용 가능 기간입니다.
 */
@Entity
@Table(name = "coupon")
@Getter
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private LocalDateTime publishFrom;

    @Column(nullable = false)
    private LocalDateTime publishUntil;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int issuedCount;

    @Column(nullable = false)
    private boolean active;

    protected Coupon() {}

    /**
     * 활성 상태, 게시 기간, 유효기간, 잔여 재고를 모두 만족할 때만 다운로드를 허용합니다.
     * 시작일과 종료일의 경계 시각은 유효한 시각으로 포함합니다.
     */
    public boolean isDownloadableAt(LocalDateTime now) {
        return active && !now.isBefore(publishFrom) && !now.isAfter(publishUntil)
                && !now.isBefore(validFrom) && !now.isAfter(validUntil) && issuedCount < stock;
    }

    public void issue() {
        // 서비스 계층에서 잠금을 획득하지만, 엔티티에서도 재고 불변식을 한 번 더 보호합니다.
        if (issuedCount >= stock) throw new IllegalStateException("쿠폰 재고가 소진되었습니다.");
        issuedCount++;
    }
}
