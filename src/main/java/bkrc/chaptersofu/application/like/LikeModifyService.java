package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.application.like.provided.LikeRegister;
import bkrc.chaptersofu.domain.like.Like;
import bkrc.chaptersofu.domain.like.LikeException;
import bkrc.chaptersofu.application.member.provided.MemberFinder;
import bkrc.chaptersofu.application.required.outbox.Outbox;
import bkrc.chaptersofu.application.required.outbox.OutboxEvent;
import bkrc.chaptersofu.application.required.outbox.OutboxRepository;
import bkrc.chaptersofu.application.support.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
            throw new LikeException(
                    ErrorCode.LIKE_ALREADY_EXISTS,
                    "operation=like, bookId=" + itemId + ", memberId=" + memberId
            );
        }

        var book = aladinBookRepository.findById(itemId)
                .orElseThrow(() -> new AladinException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + itemId));
        var member = memberFinder.getMemberById(memberId);
        Like like = likeRepository.save(Like.create(snowflake.nextId(), book, member));
        Outbox outbox = publishLikeCountChanged(itemId, memberId, 1);
        // 좋아요와 집계 이벤트 Outbox가 함께 커밋된 시점의 비즈니스 이벤트입니다.
        AfterCommitExecutor.execute(() -> log.info(
                "BOOK_LIKED likeId={} bookId={} memberId={} outboxId={}",
                like.getLikeId(), itemId, memberId, outbox.getOutboxId()
        ));
        return like;
    }

    @Override
    public void unLike(Integer itemId, Long memberId) {
        Like like = likeRepository.findByBookItemIdAndMemberMemberId(itemId, memberId)
                .orElseThrow(() -> new LikeException(
                        ErrorCode.LIKE_ALREADY_EXISTS,
                        "operation=unlike, bookId=" + itemId + ", memberId=" + memberId
                ));
        likeRepository.delete(like);
        Outbox outbox = publishLikeCountChanged(itemId, memberId, -1);
        AfterCommitExecutor.execute(() -> log.info(
                "BOOK_UNLIKED likeId={} bookId={} memberId={} outboxId={}",
                like.getLikeId(), itemId, memberId, outbox.getOutboxId()
        ));
    }

    private Outbox publishLikeCountChanged(Integer itemId, Long memberId, int delta) {
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
        return outbox;
    }
}
