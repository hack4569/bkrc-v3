package bkrc.chaptersofu.application.like;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LikeCountServiceTest {
    @SuppressWarnings("unchecked")
    @Test
    void Redis의_좋아요_수를_조회한다() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("hot-book::book::7::like-count")).thenReturn("15");

        assertThat(new LikeCountService(redis).readLikeCount(7)).isEqualTo(15L);
    }

    @Test
    void 이벤트_버전이_없으면_갱신을_거부한다() {
        LikeCountService service = new LikeCountService(mock(StringRedisTemplate.class));
        assertThatThrownBy(() -> service.createOrUpdate(7, 3, null, Duration.ofMinutes(1)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventVersion");
    }
}
