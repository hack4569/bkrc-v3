package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.application.history.provided.HistoryFinder;
import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookException;
import bkrc.chaptersofu.domain.book.BookFixture;
import bkrc.chaptersofu.domain.book.BookMall;
import bkrc.chaptersofu.domain.history.History;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 도서 캐시 조회(캐시 hit/miss)와 회원별 추천 필터링 규칙을 검증한다.
 */
class BookQueryAndRecommendationServiceTest {
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final Counter hitCounter = mock(Counter.class);
    private final Counter missCounter = mock(Counter.class);
    private final BookCacheRegister cacheRegister = mock(BookCacheRegister.class);
    private final BookQueryService queryService = new BookQueryService(bookRepository, redisTemplate,
            new ObjectMapper(), hitCounter, missCounter, cacheRegister);

    @Test
    void 캐시에_도서가_있으면_DB를_조회하지_않는다() {
        givenCache("[{\"bookId\":1,\"title\":\"데미안\"}]");

        List<Book> books = queryService.findAll();

        assertThat(books).extracting(Book::getTitle).containsExactly("데미안");
        verify(hitCounter).increment();
        verifyNoInteractions(bookRepository);
    }

    @Test
    void 캐시가_비었거나_깨졌으면_DB에서_조회해_다시_캐싱한다() {
        List<Book> dbBooks = List.of(BookFixture.book(1L, "데미안", "헤세", "소설", "2020-01-01"));
        when(bookRepository.findAllByMallName(BookMall.YES24)).thenReturn(dbBooks);

        givenCache("[]");
        assertThat(queryService.findAll()).isEqualTo(dbBooks);
        givenCache("{broken");
        assertThat(queryService.findAll()).isEqualTo(dbBooks);

        verify(missCounter, times(2)).increment();
        verify(cacheRegister, times(2)).saveListForRedis(dbBooks);
    }

    @Test
    void 비회원은_전체_도서를_추천받는다() {
        BookCacheFinder cacheFinder = mock(BookCacheFinder.class);
        HistoryFinder historyFinder = mock(HistoryFinder.class);
        List<Book> books = List.of(BookFixture.book(1L, "데미안", "헤세", "소설", "2020-01-01"));
        when(cacheFinder.findAll()).thenReturn(books);

        var service = new BookRecommendationService(cacheFinder, historyFinder, mock(HistoryRegister.class));

        assertThat(service.getRecommendBooksForUser(null)).isEqualTo(books);
        verifyNoInteractions(historyFinder);
    }

    @Test
    void 모든_도서를_이미_봤다면_이력을_초기화하고_전체를_추천한다() {
        BookCacheFinder cacheFinder = mock(BookCacheFinder.class);
        HistoryFinder historyFinder = mock(HistoryFinder.class);
        HistoryRegister historyRegister = mock(HistoryRegister.class);
        List<Book> books = List.of(BookFixture.book(1L, "데미안", "헤세", "소설", "2020-01-01"));
        History history = History.create(1L, 10L, 100L);
        ReflectionTestUtils.setField(history, "created", LocalDateTime.now().minusDays(1));
        when(cacheFinder.findAll()).thenReturn(books);
        when(historyFinder.getHistoryByMemberId(10L)).thenReturn(List.of(history));

        var service = new BookRecommendationService(cacheFinder, historyFinder, historyRegister);

        // 추천 목록이 비어 화면이 깨지지 않도록 이력을 비우고 처음부터 다시 추천한다.
        assertThat(service.getRecommendBooksForUser(10L)).isEqualTo(books);
        verify(historyRegister).deleteHistoryByMemberId(10L);
    }

    @Test
    void 추천할_도서가_없으면_예외가_발생한다() {
        BookCacheFinder cacheFinder = mock(BookCacheFinder.class);
        when(cacheFinder.findAll()).thenReturn(List.of());
        var service = new BookRecommendationService(cacheFinder, mock(HistoryFinder.class),
                mock(HistoryRegister.class));

        assertThatThrownBy(() -> service.getRecommendBooksForUser(null)).isInstanceOf(BookException.class);
    }

    private void givenCache(String value) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("books:all")).thenReturn(value);
    }
}
