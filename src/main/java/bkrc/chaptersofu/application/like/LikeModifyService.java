package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.application.messaging.RabbitMQConfig;

import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.bkrc.BookRepository;
import bkrc.chaptersofu.domain.book.BookException;
import bkrc.chaptersofu.application.required.event.EventType;
import bkrc.chaptersofu.domain.common.shared.ErrorCode;
import bkrc.chaptersofu.domain.common.shared.Snowflake;
import bkrc.chaptersofu.application.like.provided.LikeRegister;
import bkrc.chaptersofu.domain.like.Like;
import bkrc.chaptersofu.domain.like.LikeException;
import bkrc.chaptersofu.application.member.provided.MemberFinder;
import bkrc.chaptersofu.application.required.outbox.Outbox;
import bkrc.chaptersofu.application.required.outbox.OutboxEventRecorder;
import bkrc.chaptersofu.application.support.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LikeModifyService implements LikeRegister {
    private final LikeRepository likeRepository;
    private final OutboxEventRecorder eventRecorder;
    private final BookRepository bookRepository;
    private final Snowflake snowflake;
    private final MemberFinder memberFinder;

    @Override
    public Like like(Long bookId, Long memberId) {
        if (likeRepository.findByBookBookIdAndMemberMemberId(bookId, memberId).isPresent()) {
            throw new LikeException(
                    ErrorCode.LIKE_ALREADY_EXISTS,
                    "operation=like, bookId=" + bookId + ", memberId=" + memberId
            );
        }

        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));
        var member = memberFinder.getMemberById(memberId);
        Like like = likeRepository.save(Like.create(snowflake.nextId(), book, member));
        Outbox outbox = publishLikeCountChanged(bookId, memberId, 1);
        // 좋아요와 집계 이벤트 Outbox가 함께 커밋된 시점의 비즈니스 이벤트입니다.
        AfterCommitExecutor.execute(() -> log.info(
                "BOOK_LIKED likeId={} bookId={} memberId={} outboxId={}",
                like.getLikeId(), bookId, memberId, outbox.getOutboxId()
        ));
        return like;
    }

    @Override
    public void unLike(Long bookId, Long memberId) {
        Like like = likeRepository.findByBookBookIdAndMemberMemberId(bookId, memberId)
                .orElseThrow(() -> new LikeException(
                        ErrorCode.LIKE_ALREADY_EXISTS,
                        "operation=unlike, bookId=" + bookId + ", memberId=" + memberId
                ));
        likeRepository.delete(like);
        Outbox outbox = publishLikeCountChanged(bookId, memberId, -1);
        AfterCommitExecutor.execute(() -> log.info(
                "BOOK_UNLIKED likeId={} bookId={} memberId={} outboxId={}",
                like.getLikeId(), bookId, memberId, outbox.getOutboxId()
        ));
    }

    private Outbox publishLikeCountChanged(Long bookId, Long memberId, int delta) {
        return eventRecorder.record(EventType.BOOK_LIKE,
                RabbitMQConfig.HOTBOOK_DIRECT_EXCHANGE, RabbitMQConfig.LIKE_ROUTING_KEY,
                BookLikeEventPayload.builder()
                        .memberId(memberId)
                        .bookId(bookId)
                        .delta(delta)
                        .createdAt(LocalDateTime.now())
                        .build());
    }
}
