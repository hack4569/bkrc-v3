package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.adapter.payload.BookLikeEventPayload;
import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.like.application.response.LikeResponse;
import com.bkrc.bkrcv3.like.application.response.MyLikeResponse;
import com.bkrc.bkrcv3.like.entity.Like;
import com.bkrc.bkrcv3.like.entity.LikeCount;
import com.bkrc.bkrcv3.member.application.MemberRepository;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_FORMAT = "hot-book::book::%s::like-count";
    private final LikeCountRepository likeCountRepository;
    private final AladinService aladinService;
    private final AladinBookRepository aladinBookRepository;
    private final Snowflake snowflake;
    private final MemberRepository memberRepository;
    private final EntityManager em;

    @Transactional
    public LikeResponse like(Integer itemId, Long memberId) {
        if (likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        AladinBook likeItem = em.getReference(AladinBook.class, itemId);
        var memberRef = memberRepository.findById(memberId);

        Like result = likeRepository.save(Like.create(snowflake.nextId(), likeItem, memberRef.get()));

        LikeCount myLikeCount = likeCountRepository.findByItemId(itemId).orElse(LikeCount.create(itemId, 0));
        myLikeCount.increase();
        var likeCount = likeCountRepository.save(myLikeCount);

        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.BOOK_LIKE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE,
                RabbitMQConfig.LIKE_ROUTING_KEY,
                Event.of(EventType.BOOK_LIKE,
                        BookLikeEventPayload.builder()
                                .memberId(memberId)
                                .bookLikeCount(myLikeCount.getLikeCount())
                                .bookId(itemId)
                                .build()
                        ).toJson()
        ));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return LikeResponse.from(itemId, likeCount.getLikeCount());
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

    @Transactional
    public void unLike(Integer itemId, Long memberId) {
        var myLike = likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId);
        if (myLike.isEmpty()) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }
        LikeCount myLikeCount = likeCountRepository.findByItemId(itemId).orElseThrow(()-> new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS));
        myLikeCount.decrease();
        var afterLikeCount = likeCountRepository.save(myLikeCount);
        likeRepository.deleteById(myLike.get().getLikeId());
//        Like result = likeRepository.save(Like.create(snowflake.nextId(), itemId, loginId));
//
//        LikeCount myLikeCount = likeCountRepository.findByItemId(itemId).orElse(LikeCount.create(itemId, 0));
//        myLikeCount.increase();
//        var likeCount = likeCountRepository.save(myLikeCount);
//
//        Outbox outbox = outboxRepository.save(Outbox.of(
//                EventType.BOOK_LIKE,
//                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE,
//                RabbitMQConfig.LIKE_ROUTING_KEY,
//                Event.of(EventType.BOOK_LIKE,
//                        BookLikeEventPayload.builder()
//                                .loginId(loginId)
//                                .bookLikeCount(myLikeCount.getLikeCount())
//                                .bookId(itemId)
//                                .build()
//                ).toJson()
//        ));
//        eventPublisher.publishEvent(OutboxEvent.of(outbox));
//        return LikeResponse.from(result, likeCount.getLikeCount());
    }
    //좋아요 목록 조회
    public List<MyLikeResponse> getMyLikes(Long memberId) {
        var myLikeList = likeRepository.findByMemberMemberId(memberId);
        if (myLikeList.isEmpty()) return null;
        return myLikeList.get().stream()
                .map(like -> {
                    return like == null ? null : MyLikeResponse.of(like, like.getBook());
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
