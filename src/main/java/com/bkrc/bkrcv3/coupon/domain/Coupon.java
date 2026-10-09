package com.bkrc.bkrcv3.coupon.domain;

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

    public static Coupon create(String name, LocalDateTime validFrom, LocalDateTime validUntil,
                                LocalDateTime publishFrom, LocalDateTime publishUntil, int stock) {
        Coupon coupon = new Coupon();
        coupon.name = name;
        coupon.validFrom = validFrom;
        coupon.validUntil = validUntil;
        coupon.publishFrom = publishFrom;
        coupon.publishUntil = publishUntil;
        coupon.stock = stock;
        coupon.issuedCount = 0;
        coupon.active = true;
        return coupon;
    }

    /**
     * 활성 상태, 게시 기간, 유효기간을 만족하는지 확인합니다. 재고는 Redis에서 판단합니다.
     * 시작일과 종료일의 경계 시각은 유효한 시각으로 포함합니다.
     */
    public boolean isDownloadableAt(LocalDateTime now) {
        return active && !now.isBefore(publishFrom) && !now.isAfter(publishUntil)
                && !now.isBefore(validFrom) && !now.isAfter(validUntil);
    }
}
