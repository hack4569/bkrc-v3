package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.application.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.aladin.AladinBookRepository;
import bkrc.chaptersofu.domain.aladin.AladinException;
import bkrc.chaptersofu.domain.shared.event.Event;
import bkrc.chaptersofu.domain.shared.event.EventType;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.application.like.provided.LikeRegister;
import bkrc.chaptersofu.domain.like.Like;
import bkrc.chaptersofu.domain.like.LikeException;
import bkrc.chaptersofu.application.member.provided.MemberFinder;
import bkrc.chaptersofu.application.outbox.Outbox;
import bkrc.chaptersofu.application.outbox.OutboxEvent;
import bkrc.chaptersofu.application.outbox.OutboxRepository;
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
