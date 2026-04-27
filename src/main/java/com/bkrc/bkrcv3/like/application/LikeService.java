package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.adapter.payload.BookLikeEventPayload;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.like.entity.Like;
import com.bkrc.bkrcv3.like.entity.LikeCount;
import com.bkrc.bkrcv3.member.application.UserServiceImpl;
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.exception.BusinessException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_FORMAT = "hot-book::book::%s::like-count";
    private final LikeCountRepository likeCountRepository;
    private final UserServiceImpl userServiceImpl;
    private final AladinService aladinService;

    @Transactional
    public Like like(Like like){
        if (aladinService.getAladinBook(like.getItemId()) == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        if (likeRepository.findByItemIdAndLoginId(like.getItemId(), like.getLoginId()).isPresent() ) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        Like result = likeRepository.save(like);

        LikeCount myLikeCount = likeCountRepository.findByItemId(like.getItemId()).orElse(LikeCount.create(like.getItemId(), 0));
        myLikeCount.increase();
        likeCountRepository.save(myLikeCount);
        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.BOOK_LIKE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE,
                RabbitMQConfig.LIKE_ROUTING_KEY,
                Event.of(EventType.BOOK_LIKE,
                        BookLikeEventPayload.builder()
                                .loginId(like.getLoginId())
                                .bookLikeCount(myLikeCount.getLikeCount())
                                .bookId(like.getItemId())
                                .build()
                        ).toJson()
        ));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return result;
    }

    public void createOrUpdate(Integer bookId, Integer likeCount, Duration ttl) {
        redisTemplate.opsForValue().set(generateKey(bookId), String.valueOf(likeCount), ttl);
    }

    public Long read(Integer bookId) {
        String result = redisTemplate.opsForValue().get(generateKey(bookId));
        return result == null ? null : Long.valueOf(result);
    }

    private String generateKey(Integer bookId) {
        return KEY_FORMAT.formatted(bookId);
    }
}
