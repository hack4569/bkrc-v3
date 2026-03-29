package com.bkrc.bkrcv3.notification.publisher;

import com.bkrc.bkrcv3.common.dataserializer.DataSerializer;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutbox;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutboxEvent;
import com.bkrc.bkrcv3.notification.outbox.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final NotificationOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final int MAX_RETRY = 3;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEvent(NotificationOutboxEvent outboxEvent) {
        NotificationOutbox outbox = outboxEvent.getOutbox();

        if (outbox.getRetryCount() >= MAX_RETRY) {
            log.error("[Outbox] 최대 재시도 초과 outboxId={}", outbox.getOutboxId());
            return;
        }

        sendToRabbit(outbox);
    }

    // FAILED 재처리 (5분마다)
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void retryFailed() {
        List<NotificationOutbox> failedList =
                outboxRepository.findByStatus(NotificationOutbox.OutboxStatus.FAILED);

        if (failedList.isEmpty()) return;

        log.info("[Outbox] FAILED 재처리 {}건", failedList.size());
        failedList.stream()
                .filter(outbox -> outbox.getRetryCount() < MAX_RETRY)
                .forEach(this::sendToRabbit);
    }

    private void sendToRabbit(NotificationOutbox outbox) {
        try {
            rabbitTemplate.convertAndSend(
                    EventType.EXCHANGE,                    // Exchange
                    outbox.getEventType().getRoutingKey(), // Routing Key
                    outbox.getPayload()
            );
            outbox.markAsPublished();
            log.info("[RabbitMQ] 발행 성공 eventType={} outboxId={}",
                    outbox.getEventType(), outbox.getOutboxId());
        } catch (Exception e) {
            outbox.markAsFailed();
            log.error("[RabbitMQ] 발행 실패 eventType={} outboxId={}",
                    outbox.getEventType(), outbox.getOutboxId(), e);
        }
    }
}