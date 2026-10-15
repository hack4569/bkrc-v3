package bkrc.chaptersofu.application.outbox;

import bkrc.chaptersofu.application.required.outbox.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OutboxRecoverySchedulerTest {
    @Test
    void 오래된_PENDING_아웃박스를_상태_변경_없이_재발행한다() {
        OutboxRepository repository = mock(OutboxRepository.class);
        OutboxPublisher publisher = mock(OutboxPublisher.class);
        Outbox outbox = mock(Outbox.class);
        when(repository.findTop100ByOutboxStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                eq(OutboxStatus.PENDING), any())).thenReturn(List.of(outbox));

        new OutboxRecoveryScheduler(repository, publisher).retryPendingOutboxes();

        verify(publisher).publish(outbox);
    }

    @Test
    void 재처리할_아웃박스가_없으면_아무것도_발행하지_않는다() {
        OutboxRepository repository = mock(OutboxRepository.class);
        OutboxPublisher publisher = mock(OutboxPublisher.class);
        when(repository.findTop100ByOutboxStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                eq(OutboxStatus.PENDING), any())).thenReturn(List.of());

        new OutboxRecoveryScheduler(repository, publisher).retryPendingOutboxes();

        verifyNoInteractions(publisher);
    }
}
