package bkrc.chaptersofu.adapter.webapi;

import bkrc.chaptersofu.adapter.webapi.dto.CouponResponse;
import bkrc.chaptersofu.application.coupon.provided.CouponFinder;
import bkrc.chaptersofu.application.coupon.provided.CouponRegister;
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
        return couponFinder.getDownloadable().stream()
                .map(CouponResponse::from)
                .toList();
    }

    @PostMapping("/{couponId}/download")
    public CouponResponse.Issued download(@PathVariable Long couponId,
                                          @AuthenticationPrincipal Long memberId) {
        var memberCoupon = couponRegister.download(couponId, memberId);
        return CouponResponse.Issued.from(memberCoupon, couponFinder.getCoupon(memberCoupon.getCouponId()));
    }

    @GetMapping("/my")
    public List<CouponResponse.Issued> mine(@AuthenticationPrincipal Long memberId) {
        return couponFinder.getMine(memberId).stream()
                .map(memberCoupon -> CouponResponse.Issued.from(
                        memberCoupon, couponFinder.getCoupon(memberCoupon.getCouponId())))
                .toList();
    }

    @PostMapping("/my/{memberCouponId}/use")
    public CouponResponse.Issued use(@PathVariable Long memberCouponId,
                                     @AuthenticationPrincipal Long memberId) {
        var memberCoupon = couponRegister.use(memberCouponId, memberId);
        return CouponResponse.Issued.from(memberCoupon, couponFinder.getCoupon(memberCoupon.getCouponId()));
    }
}
