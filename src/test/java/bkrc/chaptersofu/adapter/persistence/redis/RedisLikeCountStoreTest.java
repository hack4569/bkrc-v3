package bkrc.chaptersofu.adapter.persistence.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisLikeCountStoreTest {
    @SuppressWarnings("unchecked")
    @Test
    void Redis의_좋아요_수를_조회한다() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("hot-book::book::7::like-count")).thenReturn("15");

        assertThat(new RedisLikeCountStore(redis).readLikeCount(7)).isEqualTo(15L);
    }

}
