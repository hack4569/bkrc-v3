package com.bkrc.bkrcv3.coupon.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 사용자용 쿠폰 조회, 다운로드 및 사용 API입니다. */
@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping
    // 공개 API: 로그인하지 않은 사용자도 현재 받을 수 있는 쿠폰을 볼 수 있습니다.
    public List<CouponResponse> coupons() { return couponService.getDownloadable(); }

    @PostMapping("/{couponId}/download")
    // 인증 필터가 JWT subject에 담긴 불변 회원 PK를 memberId로 전달합니다.
    public CouponResponse.Issued download(@PathVariable Long couponId, @AuthenticationPrincipal Long memberId) {
        return couponService.download(couponId, memberId);
    }

    @GetMapping("/my")
    public List<CouponResponse.Issued> mine(@AuthenticationPrincipal Long memberId) {
        return couponService.getMine(memberId);
    }

    @PostMapping("/my/{memberCouponId}/use")
    public CouponResponse.Issued use(@PathVariable Long memberCouponId, @AuthenticationPrincipal Long memberId) {
        return couponService.use(memberCouponId, memberId);
    }
}
