package com.bkrc.bkrcv3.outbox.publisher;

import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import com.bkrc.bkrcv3.outbox.OutboxStatusUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProducer {

    private final OutboxRepository outboxRepository;
    private final OutboxStatusUpdater outboxStatusUpdater;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final int MAX_RETRY = 3;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEvent(OutboxEvent outboxEvent) {
        Outbox outbox = outboxEvent.getOutbox();

        if (outbox.getRetryCount() >= MAX_RETRY) {
            log.error("[Outbox] 최대 재시도 초과 outboxId={}", outbox.getOutboxId());
            return;
        }

        sendToKafka(outbox);
    }

    private void sendToKafka(Outbox outbox) {
        try {
            kafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                            outbox.getShardKey(),
                            outbox.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            outboxStatusUpdater.markAsFailed(outbox.getOutboxId());
                            log.error("[Outbox] 발행 실패 eventType={} outboxId={}",
                                    outbox.getEventType(), outbox.getOutboxId());
                        } else {
                            log.info("[Outbox] 발행 성공 eventType={} outboxId={}",
                                    outbox.getEventType(), outbox.getOutboxId());
                            outboxStatusUpdater.delete(outbox.getOutboxId());
                        }
                    });
        } catch (Exception e) {
            log.error("[OutboxProducer sendToKafka error! = {}]", e.getMessage(), e);
            outboxStatusUpdater.markAsFailed(outbox.getOutboxId());
        }

    }
}
