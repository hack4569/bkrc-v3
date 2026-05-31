package com.bkrc.bkrcv3.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxStatusUpdater {

    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long outboxId) {
        outboxRepository.deleteById(outboxId);
    }

    public void markFailed(Long outboxId) {
        outboxRepository.findById(outboxId).ifPresent(outbox -> {
            outbox.markFailed();  // status = FAILED, retry_count++
        });
    }
}
