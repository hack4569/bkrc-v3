package bkrc.chaptersofu.application.aladin.cache;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AladinBookCacheTest {
    @SuppressWarnings("unchecked")
    @Test
    void 캐시가_비어있으면_miss를_기록한다() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        Counter hit = mock(Counter.class);
        Counter miss = mock(Counter.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("aladin:books:all")).thenReturn(null);

        assertThat(new AladinBookCache(redis, mock(ObjectMapper.class), hit, miss).find()).isEmpty();
        verify(miss).increment();
        verifyNoInteractions(hit);
    }

    @SuppressWarnings("unchecked")
    @Test
    void 캐시_JSON을_도서_목록으로_변환하고_hit를_기록한다() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        Counter hit = mock(Counter.class);
        Counter miss = mock(Counter.class);
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("aladin:books:all")).thenReturn("json");
        when(mapper.readValue(eq("json"), any(TypeReference.class))).thenReturn(books);

        assertThat(new AladinBookCache(redis, mapper, hit, miss).find()).contains(books);
        verify(hit).increment();
        verifyNoInteractions(miss);
    }

    @SuppressWarnings("unchecked")
    @Test
    void 역직렬화에_실패하면_miss로_처리한다() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        Counter miss = mock(Counter.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("aladin:books:all")).thenReturn("broken");
        when(mapper.readValue(eq("broken"), any(TypeReference.class)))
                .thenThrow(mock(JsonProcessingException.class));

        assertThat(new AladinBookCache(redis, mapper, mock(Counter.class), miss).find()).isEmpty();
        verify(miss).increment();
    }
}
