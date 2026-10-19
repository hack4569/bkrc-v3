package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.application.aladin.provided.AladinBookCacheFinder;
import bkrc.chaptersofu.application.aladin.provided.AladinBookCacheRegister;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AladinBookQueryService implements AladinBookCacheFinder {

    private static final String CACHE_KEY_ALL_BOOKS = "aladin:books:all";

    private final AladinBookRepository aladinBookRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final AladinBookCacheRegister aladinBookCacheRegister;

    @Override
    public List<AladinBook> findAll() {
        return findFromCache()
                .orElseGet(() -> {
                    List<AladinBook> books = aladinBookRepository.findAllWithBookComments();
                    aladinBookCacheRegister.saveListForRedis(books);
                    return books;
                });
    }

    private Optional<List<AladinBook>> findFromCache() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL_BOOKS);

        if (!StringUtils.hasText(cached)) {
            cacheMissCounter.increment();
            return Optional.empty();
        }

        try {
            List<AladinBook> books = objectMapper.readValue(
                    cached,
                    new TypeReference<List<AladinBook>>() {}
            );
            cacheHitCounter.increment();
            return Optional.of(books);
        } catch (JsonProcessingException e) {
            cacheMissCounter.increment();
            log.warn("[알라딘] 캐시 역직렬화 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
            return Optional.empty();
        }
    }
}
