package com.bkrc.bkrcv3.adapter.payload;

import com.bkrc.bkrcv3.required.EventPayload;
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
