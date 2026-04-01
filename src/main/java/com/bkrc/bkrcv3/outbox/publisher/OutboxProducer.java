package com.bkrc.bkrcv3.outbox.publisher;

import com.bkrc.bkrcv3.outbox.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.outbox.OutboxRepository;
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
                            outbox.markAsFailed();
                            log.error("[Outbox] 발행 실패 eventType={} outboxId={}",
                                    outbox.getEventType(), outbox.getOutboxId());
                        } else {
                            outbox.markAsPublished();
                            log.info("[Outbox] 발행 성공 eventType={} outboxId={}",
                                    outbox.getEventType(), outbox.getOutboxId());
                            outboxRepository.delete(outbox);
                        }
                    });
        } catch (Exception e) {
            log.error("[OutboxProducer sendToKafka error! = {}]", e.getMessage(), e);
            outbox.markAsFailed();
        }
    }
}
