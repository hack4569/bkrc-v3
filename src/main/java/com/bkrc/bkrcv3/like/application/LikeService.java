package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.adapter.payload.BookLikeEventPayload;
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
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
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
    private final Snowflake snowflake;
    private final MemberRepository memberRepository;

    @Transactional
    public LikeResponse like(Integer itemId, String loginId) {
//        if (aladinService.getAladinBook(itemId) == null) {
//            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
//        }
//        if (likeRepository.findByItemIdAndLoginId(itemId, loginId).isPresent() ) {
//            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
//        }

        var member = memberRepository.findMemberByLoginId(loginId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Like result = likeRepository.save(Like.create(snowflake.nextId(), itemId, member));

        LikeCount myLikeCount = likeCountRepository.findByItemId(itemId).orElse(LikeCount.create(itemId, 0));
        myLikeCount.increase();
        var likeCount = likeCountRepository.save(myLikeCount);

        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.BOOK_LIKE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE,
                RabbitMQConfig.LIKE_ROUTING_KEY,
                Event.of(EventType.BOOK_LIKE,
                        BookLikeEventPayload.builder()
                                .loginId(loginId)
                                .bookLikeCount(myLikeCount.getLikeCount())
                                .bookId(itemId)
                                .build()
                        ).toJson()
        ));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return LikeResponse.from(result, likeCount.getLikeCount());
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
    public void unLike(Integer itemId, String loginId) {
        Like myLike = likeRepository.findByItemIdAndMemberLoginId(itemId, loginId).orElseThrow(() -> new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS));
        LikeCount myLikeCount = likeCountRepository.findByItemId(myLike.getItemId()).orElseThrow(()-> new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS));
        myLikeCount.decrease();
        var afterLikeCount = likeCountRepository.save(myLikeCount);
        likeRepository.deleteById(myLike.getLikeId());
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
    public List<MyLikeResponse> getMyLikes(String loginId) {
        List<Like> myLikeList = likeRepository.findByMemberLoginId(loginId);
        if (myLikeList.isEmpty()) return null;
        List<Integer> itemIds = myLikeList.stream().map(Like::getItemId).toList();
        Map<Integer, AladinBook> bookMap = aladinService.getAladinBooksByItemIds(itemIds).stream()
                .collect(Collectors.toMap(AladinBook::getItemId, Function.identity()));
        return myLikeList.stream()
                .map(like -> {
                    AladinBook book = bookMap.get(like.getItemId());
                    return book == null ? null : MyLikeResponse.of(like, book);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
