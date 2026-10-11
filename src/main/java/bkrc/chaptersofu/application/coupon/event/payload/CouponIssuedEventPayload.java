package bkrc.chaptersofu.application.coupon.event.payload;

import bkrc.chaptersofu.application.shared.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssuedEventPayload implements EventPayload {
    private Long memberCouponId;
    private Long couponId;
    private Long memberId;
    private LocalDateTime issuedAt;
}
