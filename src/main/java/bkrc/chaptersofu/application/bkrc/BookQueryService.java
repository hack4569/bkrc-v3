package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.application.aladin.provided.AladinBookCacheFinder;
import bkrc.chaptersofu.application.aladin.provided.AladinBookCacheRegister;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;
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
public class BookQueryService implements BookCacheFinder {

    private static final String CACHE_KEY_ALL_BOOKS = "books:all";

    private final BookRepository bookRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final BookCacheRegister bookCacheRegister;

    @Override
    public List<Book> findAll() {
        return findFromCache()
                .orElseGet(() -> {
                    // 현재 공통 도서 캐시는 YES24 도서만 제공하므로 DB 조회 단계에서 판매처를 제한합니다.
                    List<Book> books = bookRepository.findAllByMallName(BookMall.YES24);
                    bookCacheRegister.saveListForRedis(books);
                    return books;
                });
    }

    private Optional<List<Book>> findFromCache() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL_BOOKS);

        // JSON 빈 배열도 조회할 도서가 없는 캐시로 간주해 최신 DB 결과를 다시 확인한다.
        if (!StringUtils.hasText(cached) || "[]".equals(cached.strip())) {
            cacheMissCounter.increment();
            return Optional.empty();
        }

        try {
            List<Book> books = objectMapper.readValue(
                    cached,
                    new TypeReference<List<Book>>() {}
            );
            cacheHitCounter.increment();
            return Optional.of(books);
        } catch (JsonProcessingException e) {
            cacheMissCounter.increment();
            log.warn("캐시 역직렬화 실패. key={}", CACHE_KEY_ALL_BOOKS, e);
            return Optional.empty();
        }
    }
}
