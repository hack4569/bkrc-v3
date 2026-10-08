package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.application.provided.LikeCountFinder;
import com.bkrc.bkrcv3.like.application.provided.LikeCountUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeCountService implements LikeCountFinder, LikeCountUpdater {
    private static final String KEY_FORMAT = "hot-book::book::%s::like-count";
    private static final DefaultRedisScript<Long> APPLY_LIKE_COUNT_SCRIPT = new DefaultRedisScript<>("""
            local currentVersion = redis.call('GET', KEYS[2])
            if currentVersion and tonumber(ARGV[2]) <= tonumber(currentVersion) then
                return 0
            end
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[3])
            redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public Long readLikeCount(Integer bookId) {
        String result = redisTemplate.opsForValue().get(generateKey(bookId));
        return result == null ? null : Long.valueOf(result);
    }

    @Override
    public boolean createOrUpdate(Integer bookId, Integer likeCount, Long eventVersion, Duration ttl) {
        Objects.requireNonNull(eventVersion, "BOOK_LIKE eventVersion must not be null");
        Long applied = redisTemplate.execute(
                APPLY_LIKE_COUNT_SCRIPT,
                List.of(generateKey(bookId), generateVersionKey(bookId)),
                String.valueOf(likeCount),
                String.valueOf(eventVersion),
                String.valueOf(ttl.toSeconds())
        );
        return Long.valueOf(1L).equals(applied);
    }

    private String generateKey(Integer bookId) {
        return KEY_FORMAT.formatted(bookId);
    }

    private String generateVersionKey(Integer bookId) {
        return generateKey(bookId) + "::version";
    }
}
