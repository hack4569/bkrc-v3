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
import org.springframework.data.redis.core.script.DefaultRedisScript;
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

    private static final DefaultRedisScript<Long> APPLY_LIKE_COUNT_SCRIPT = new DefaultRedisScript<>("""
            local currentVersion = redis.call('GET', KEYS[2])
            if currentVersion and tonumber(ARGV[2]) <= tonumber(currentVersion) then
                return 0
            end
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[3])
            redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3])
            return 1
            """, Long.class);

    @Transactional
    public LikeResponse like(Integer itemId, Long memberId) {
        if (likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        AladinBook likeItem = aladinService.getAladinBook(itemId);
        var memberRef = memberRepository.findById(memberId);

        likeRepository.save(Like.create(snowflake.nextId(), likeItem, memberRef.get()));

        LikeCount myLikeCount = likeCountRepository.findByItemId(itemId).orElse(LikeCount.create(itemId, 0));
        myLikeCount.increase();
        likeCountRepository.save(myLikeCount);
        publishLikeCountChanged(myLikeCount, memberId);
        return LikeResponse.from(itemId, myLikeCount.getLikeCount());
    }

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

    public Long read(Integer bookId) {
        String result = redisTemplate.opsForValue().get(generateKey(bookId));
        return result == null ? null : Long.valueOf(result);
    }

    private String generateKey(Integer bookId) {
        return KEY_FORMAT.formatted(bookId);
    }

    private String generateVersionKey(Integer bookId) {
        return generateKey(bookId) + "::version";
    }

    @Transactional
    public void unLike(Integer itemId, Long memberId) {
        var myLike = likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId);
        if (myLike.isEmpty()) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }
        LikeCount myLikeCount = likeCountRepository.findByItemId(itemId).orElseThrow(()-> new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS));
        myLikeCount.decrease();
        likeCountRepository.save(myLikeCount);
        likeRepository.deleteById(myLike.get().getLikeId());
        publishLikeCountChanged(myLikeCount, memberId);
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

    private void publishLikeCountChanged(LikeCount likeCount, Long memberId) {
        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.BOOK_LIKE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE,
                RabbitMQConfig.LIKE_ROUTING_KEY,
                Event.of(EventType.BOOK_LIKE,
                        BookLikeEventPayload.builder()
                                .memberId(memberId)
                                .bookLikeCount(likeCount.getLikeCount())
                                .bookId(likeCount.getItemId())
                                .eventVersion(likeCount.getEventVersion())
                                .build()
                ).toJson()
        ));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
    //좋아요 목록 조회
    public List<MyLikeResponse> getMyLikes(Member member) {
        var myLikeList = member.getMyLikes();
        if (myLikeList.isEmpty()) return null;
        return myLikeList.stream()
                .map(like -> {
                    return like == null ? null : MyLikeResponse.of(like, like.getBook());
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
