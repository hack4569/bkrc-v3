package bkrc.chaptersofu.application.hotbook;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 날짜별 인기 도서 ZSET의 키 규칙과 상위 N개 유지 로직을 검증한다.
 */
class HotBookRepositoryTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final HotBookRepository repository = new HotBookRepository(redisTemplate);

    @Test
    @SuppressWarnings("unchecked")
    void 점수를_추가하고_상위_N개만_남기며_만료시간을_설정한다() {
        repository.add(7L, LocalDateTime.of(2026, 11, 28, 10, 0), 5L, 10L, Duration.ofDays(1));

        // 파이프라인 콜백을 캡처해 목 커넥션으로 직접 실행하여 Redis 명령을 검증한다.
        ArgumentCaptor<RedisCallback<?>> captor = ArgumentCaptor.forClass(RedisCallback.class);
        verify(redisTemplate).executePipelined(captor.capture());
        StringRedisConnection connection = mock(StringRedisConnection.class);
        captor.getValue().doInRedis(connection);

        String key = "hot-book::list::20261128";
        verify(connection).zAdd(key, 5L, "7");
        // 낮은 점수부터 제거해 상위 limit개만 유지해야 한다.
        verify(connection).zRemRange(key, 0, -11);
        verify(connection).expire(key, 86400L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 점수가_높은_순서로_도서_ID를_조회한다() {
        ZSetOperations<String, String> zSet = mock(ZSetOperations.class);
        var tuples = new LinkedHashSet<ZSetOperations.TypedTuple<String>>(List.of(
                new DefaultTypedTuple<>("3", 10.0), new DefaultTypedTuple<>("1", 5.0)));
        when(redisTemplate.opsForZSet()).thenReturn(zSet);
        when(zSet.reverseRangeWithScores("hot-book::list::20261128", 0, -1)).thenReturn(tuples);

        assertThat(repository.readAll("20261128")).containsExactly(3L, 1L);
    }
}
