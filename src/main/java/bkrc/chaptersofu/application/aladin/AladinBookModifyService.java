package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.application.aladin.provided.AladinBookCacheRegister;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AladinBookModifyService implements AladinBookCacheRegister {

    private static final String CACHE_KEY_ALL_BOOKS = "aladin:books:all";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveListForRedis(List<AladinBook> books) {
        try {
            String json = objectMapper.writeValueAsString(books);
            System.out.println("Redis 캐시 저장 데이터: " + json);
            redisTemplate.opsForValue().set(CACHE_KEY_ALL_BOOKS, json, CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("[알라딘] 캐시 저장 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
        }
    }
}
