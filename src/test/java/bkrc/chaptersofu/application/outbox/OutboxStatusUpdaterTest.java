package bkrc.chaptersofu.application.outbox;

import bkrc.chaptersofu.application.shared.event.EventType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxStatusUpdaterTest {
    private final OutboxRepository repository = mock(OutboxRepository.class);
    private final OutboxStatusUpdater updater = new OutboxStatusUpdater(repository);

    @Test
    void 아웃박스를_삭제한다() {
        updater.delete(3L);
        verify(repository).deleteById(3L);
    }

    @Test
    void 아웃박스를_실패_상태로_변경한다() {
        Outbox outbox = Outbox.of(EventType.BOOK_LIKE, "exchange", "key", "payload");
        updater.markFailed(outbox);
        assertThat(outbox.getOutboxStatus()).isEqualTo(OutboxStatus.FAILED);
    }
}
