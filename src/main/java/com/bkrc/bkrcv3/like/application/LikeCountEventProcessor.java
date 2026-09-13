package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.adapter.payload.BookLikeEventPayload;
import com.bkrc.bkrcv3.like.entity.LikeCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeCountEventProcessor {

    private final LikeCountRepository likeCountRepository;
    private final LikeService likeService;
    private final TransactionTemplate transactionTemplate;

    public void process(BookLikeEventPayload payload, Duration redisTtl) {
        validate(payload);

        LikeCountSnapshot snapshot = transactionTemplate.execute(status -> {
            likeCountRepository.applyDelta(payload.getBookId(), payload.getDelta());

            LikeCount count = likeCountRepository.findById(payload.getBookId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Like count was not created. itemId=" + payload.getBookId()
                    ));
            return new LikeCountSnapshot(count.getLikeCount(), count.getEventVersion());
        });

        Objects.requireNonNull(snapshot, "Like count transaction returned null");
        likeService.createOrUpdate(
                payload.getBookId(),
                snapshot.likeCount(),
                snapshot.eventVersion(),
                redisTtl
        );
    }

    private void validate(BookLikeEventPayload payload) {
        Objects.requireNonNull(payload.getBookId(), "BOOK_LIKE bookId must not be null");
        if (!Integer.valueOf(1).equals(payload.getDelta())
                && !Integer.valueOf(-1).equals(payload.getDelta())) {
            throw new IllegalArgumentException("BOOK_LIKE delta must be 1 or -1");
        }
    }

    private record LikeCountSnapshot(Integer likeCount, Long eventVersion) {
    }
}
