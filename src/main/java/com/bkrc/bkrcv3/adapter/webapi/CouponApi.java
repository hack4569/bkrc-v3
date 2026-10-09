package com.bkrc.bkrcv3.adapter.webapi;

import com.bkrc.bkrcv3.coupon.application.CouponResponse;
import com.bkrc.bkrcv3.coupon.application.provided.CouponFinder;
import com.bkrc.bkrcv3.coupon.application.provided.CouponRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponApi {
    private final CouponFinder couponFinder;
    private final CouponRegister couponRegister;

    @GetMapping
    public List<CouponResponse> coupons() {
        return couponFinder.getDownloadable();
    }

    @PostMapping("/{couponId}/download")
    public CouponResponse.Issued download(@PathVariable Long couponId,
                                          @AuthenticationPrincipal Long memberId) {
        return couponRegister.download(couponId, memberId);
    }

    @GetMapping("/my")
    public List<CouponResponse.Issued> mine(@AuthenticationPrincipal Long memberId) {
        return couponFinder.getMine(memberId);
    }

    @PostMapping("/my/{memberCouponId}/use")
    public CouponResponse.Issued use(@PathVariable Long memberCouponId,
                                     @AuthenticationPrincipal Long memberId) {
        return couponRegister.use(memberCouponId, memberId);
    }
}
