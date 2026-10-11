package bkrc.chaptersofu.application.like;

import bkrc.chaptersofu.application.like.event.payload.BookLikeEventPayload;
import bkrc.chaptersofu.application.like.provided.LikeCountUpdater;
import bkrc.chaptersofu.domain.like.LikeCount;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LikeCountEventProcessorTest {
    @SuppressWarnings("unchecked")
    @Test
    void DB_집계_후_같은_버전으로_Redis를_갱신한다() {
        LikeCountRepository repository = mock(LikeCountRepository.class);
        LikeCountUpdater updater = mock(LikeCountUpdater.class);
        TransactionTemplate transaction = mock(TransactionTemplate.class);
        LikeCount count = LikeCount.create(7, 3);
        count.increase();
        when(repository.findById(7)).thenReturn(Optional.of(count));
        when(transaction.execute(any())).thenAnswer(invocation ->
                invocation.<TransactionCallback<?>>getArgument(0)
                        .doInTransaction(mock(TransactionStatus.class)));
        BookLikeEventPayload payload = BookLikeEventPayload.builder().bookId(7).delta(1).build();
        Duration ttl = Duration.ofMinutes(5);

        new LikeCountEventProcessor(repository, updater, transaction).process(payload, ttl);

        verify(repository).applyDelta(7, 1);
        verify(updater).createOrUpdate(7, 4, 1L, ttl);
    }

    @Test
    void delta가_1이나_minus1이_아니면_거부한다() {
        LikeCountEventProcessor processor = new LikeCountEventProcessor(
                mock(LikeCountRepository.class), mock(LikeCountUpdater.class), mock(TransactionTemplate.class));
        BookLikeEventPayload payload = BookLikeEventPayload.builder().bookId(7).delta(2).build();

        assertThatThrownBy(() -> processor.process(payload, Duration.ofMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("delta");
    }
}
