package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.application.aladin.AladinBookQueryService;
import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AladinBookCacheFinderTest {

    @Mock AladinBookRepository aladinBookRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock ObjectMapper objectMapper;
    @Mock Counter cacheHitCounter;
    @Mock Counter cacheMissCounter;
    @Mock AladinBookCacheRegister aladinBookCacheRegister;

    private AladinBookCacheFinder aladinBookCacheFinder;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        aladinBookCacheFinder = new AladinBookQueryService(
                aladinBookRepository,
                redisTemplate,
                objectMapper,
                cacheHitCounter,
                cacheMissCounter,
                aladinBookCacheRegister
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void 캐시에_도서가_있으면_DB를_조회하지_않는다() throws Exception {
        List<AladinBook> cachedBooks = List.of(mock(AladinBook.class));
        when(valueOperations.get("aladin:books:all")).thenReturn("json");
        when(objectMapper.readValue(eq("json"), any(TypeReference.class))).thenReturn(cachedBooks);

        assertThat(aladinBookCacheFinder.findAll()).isSameAs(cachedBooks);
        verify(cacheHitCounter).increment();
        verifyNoInteractions(cacheMissCounter, aladinBookRepository, aladinBookCacheRegister);
    }

    @Test
    void 캐시가_비어있으면_DB_결과를_캐시에_저장한다() {
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(valueOperations.get("aladin:books:all")).thenReturn(null);
        when(aladinBookRepository.findAllWithBookComments()).thenReturn(books);

        assertThat(aladinBookCacheFinder.findAll()).isSameAs(books);
        verify(cacheMissCounter).increment();
        verify(aladinBookCacheRegister).saveListForRedis(books);
        verifyNoInteractions(cacheHitCounter);
    }

    @SuppressWarnings("unchecked")
    @Test
    void 캐시_역직렬화에_실패하면_DB_결과를_사용한다() throws Exception {
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(valueOperations.get("aladin:books:all")).thenReturn("broken");
        when(objectMapper.readValue(eq("broken"), any(TypeReference.class)))
                .thenThrow(mock(JsonProcessingException.class));
        when(aladinBookRepository.findAllWithBookComments()).thenReturn(books);

        assertThat(aladinBookCacheFinder.findAll()).isSameAs(books);
        verify(cacheMissCounter).increment();
        verify(aladinBookCacheRegister).saveListForRedis(books);
    }
}
