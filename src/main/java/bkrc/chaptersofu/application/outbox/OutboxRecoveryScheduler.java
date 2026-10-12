package bkrc.chaptersofu.application.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRecoveryScheduler {

    private static final int RETRY_DELAY_MINUTES = 5;

    private final OutboxRepository outboxRepository;
    private final OutboxPublisher outboxPublisher;

    @Scheduled(fixedDelayString = "${outbox.recovery.fixed-delay-ms:300000}")
    @SchedulerLock(name = "outboxRecoveryScheduler", lockAtMostFor = "PT4M")
    public void retryPendingOutboxes() {
        LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES);

        List<Outbox> targets = outboxRepository
                .findTop100ByOutboxStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                        OutboxStatus.PENDING,
                        retryThreshold
                );

        if (!targets.isEmpty()) {
            log.info("Retrying pending outboxes - count: {}", targets.size());
        }
        // ACK를 받기 전까지 PENDING 상태를 유지한다.
        // NACK, 발행 예외, Confirm 응답 유실 시 다음 스케줄에서 다시 조회해 재발행한다.
        targets.forEach(outboxPublisher::publish);
    }
}
