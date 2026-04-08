package com.bkrc.bkrcv3.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    // 캐시 HIT/MISS 카운터
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("bkrc.cache.hit")
                .description("Redis 캐시 HIT 횟수")
                .register(registry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("bkrc.cache.miss")
                .description("Redis 캐시 MISS 횟수")
                .register(registry);
    }
}
