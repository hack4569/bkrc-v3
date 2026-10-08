package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.adapter.payload.BookLikeEventPayload;
import com.bkrc.bkrcv3.aladin.application.AladinBookRepository;
import com.bkrc.bkrcv3.aladin.entity.AladinException;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.common.shared.ErrorCode;
import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.like.application.provided.LikeRegister;
import com.bkrc.bkrcv3.like.domain.Like;
import com.bkrc.bkrcv3.like.domain.LikeException;
import com.bkrc.bkrcv3.member.application.provided.MemberFinder;
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeModifyService implements LikeRegister {
    private final LikeRepository likeRepository;
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AladinBookRepository aladinBookRepository;
    private final Snowflake snowflake;
    private final MemberFinder memberFinder;

    @Override
    public Like like(Integer itemId, Long memberId) {
        if (likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId).isPresent()) {
            throw new LikeException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        var book = aladinBookRepository.findById(itemId)
                .orElseThrow(() -> new AladinException(ErrorCode.BOOK_NOT_FOUND));
        var member = memberFinder.getMemberById(memberId);
        Like like = likeRepository.save(Like.create(snowflake.nextId(), book, member));
        publishLikeCountChanged(itemId, memberId, 1);
        return like;
    }

    @Override
    public void unLike(Integer itemId, Long memberId) {
        Like like = likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId)
                .orElseThrow(() -> new LikeException(ErrorCode.LIKE_ALREADY_EXISTS));
        likeRepository.delete(like);
        publishLikeCountChanged(itemId, memberId, -1);
    }

    private void publishLikeCountChanged(Integer itemId, Long memberId, int delta) {
        Outbox outbox = outboxRepository.save(Outbox.of(
                EventType.BOOK_LIKE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE,
                RabbitMQConfig.LIKE_ROUTING_KEY,
                Event.of(EventType.BOOK_LIKE,
                        BookLikeEventPayload.builder()
                                .memberId(memberId)
                                .bookId(itemId)
                                .delta(delta)
                                .createdAt(LocalDateTime.now())
                                .build()
                ).toJson()
        ));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
