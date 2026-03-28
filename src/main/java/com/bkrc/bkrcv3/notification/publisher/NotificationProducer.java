package com.bkrc.bkrcv3.notification.publisher;

import com.bkrc.bkrcv3.common.dataserializer.DataSerializer;
import com.bkrc.bkrcv3.common.event.payload.EventPayload;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutbox;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutboxEvent;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutboxRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final NotificationOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final int MAX_RETRY = 3;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEvent(NotificationOutboxEvent outboxEvent) {
        NotificationOutbox outbox = outboxEvent.getOutbox();

        if (outbox.getRetryCount() >= MAX_RETRY) {
            log.error("[Outbox] 최대 재시도 초과 outboxId={}", outbox.getOutboxId());
            return;
        }

        sendToKafka(outbox);
    }
//    @Scheduled(fixedDelay = 5000)
    @Transactional
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish() {
        List<NotificationOutbox> pendingList =
                outboxRepository.findByStatus(NotificationOutbox.OutboxStatus.PENDING);

        if (pendingList.isEmpty()) return;

        pendingList.stream()
                .filter(outbox -> outbox.getRetryCount() < MAX_RETRY)
                .forEach(this::sendToKafka);
    }

    private void sendToKafka(NotificationOutbox outbox) {
        try {
            kafkaTemplate.send(
                    outbox.getEventType().getTopic(),
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
            log.error("[NotificationProducer sendToKafka error! = {}]", e.getMessage(), e);
            outbox.markAsFailed();
        }
    }
}
