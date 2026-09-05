package com.bkrc.bkrcv3.outbox.publisher;

import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import com.bkrc.bkrcv3.outbox.OutboxStatus;
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
    private final OutboxProducer outboxProducer;

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
        targets.forEach(outboxProducer::publishFinalAttempt);
    }
}
