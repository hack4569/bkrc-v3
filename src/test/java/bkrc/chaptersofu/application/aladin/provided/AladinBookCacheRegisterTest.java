package bkrc.chaptersofu.application.aladin.provided;

import bkrc.chaptersofu.application.aladin.AladinBookModifyService;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AladinBookCacheRegisterTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock ObjectMapper objectMapper;

    private AladinBookCacheRegister aladinBookCacheRegister;

    @BeforeEach
    void setUp() {
        aladinBookCacheRegister = new AladinBookModifyService(redisTemplate, objectMapper);
    }

    @Test
    void 도서_목록을_JSON으로_변환해_Redis에_저장한다() throws Exception {
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(objectMapper.writeValueAsString(books)).thenReturn("json");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        aladinBookCacheRegister.saveListForRedis(books);

        verify(valueOperations).set("aladin:books:all", "json", Duration.ofHours(24));
    }

    @Test
    void 직렬화에_실패하면_Redis에_저장하지_않는다() throws Exception {
        List<AladinBook> books = List.of(mock(AladinBook.class));
        when(objectMapper.writeValueAsString(books))
                .thenThrow(mock(JsonProcessingException.class));

        aladinBookCacheRegister.saveListForRedis(books);

        verifyNoInteractions(valueOperations);
    }
}
