package com.bkrc.bkrcv3.aladin.infrastructure.cache;

import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.application.AladinMapper;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AladinBookCache {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private static final String CACHE_KEY_ALL_BOOKS = "aladin:books:all";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    public Optional<AladinBookPageResponse> find() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL_BOOKS);

        if (!StringUtils.hasText(cached)) {
            cacheMissCounter.increment();
            return Optional.empty();
        }

        try {
            AladinBookPageResponse response =
                    objectMapper.readValue(cached, AladinBookPageResponse.class);

            cacheHitCounter.increment();
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            cacheMissCounter.increment();
            log.warn("[알라딘] 캐시 역직렬화 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
            return Optional.empty();
        }
    }

    public void save(AladinBookPageResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue()
                    .set(CACHE_KEY_ALL_BOOKS, json, CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("[알라딘] 캐시 저장 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
        }
    }
}
