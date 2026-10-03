package com.bkrc.bkrcv3.coupon.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class CouponConfig {
    /**
     * 쿠폰 정책의 기준 시각입니다. Clock 주입 방식은 테스트에서 고정 시각으로 교체할 수 있게 해줍니다.
     */
    @Bean
    Clock couponClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
