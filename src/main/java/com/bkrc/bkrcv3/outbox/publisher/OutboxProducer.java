package com.bkrc.bkrcv3.outbox.publisher;

import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.outbox.Outbox;
import com.bkrc.bkrcv3.outbox.OutboxEvent;
import com.bkrc.bkrcv3.outbox.OutboxStatusUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProducer {

    private final OutboxStatusUpdater outboxStatusUpdater;
    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEvent(OutboxEvent outboxEvent) {
        Outbox outbox = outboxEvent.getOutbox();

        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_DIRECT_EXCHANGE, outbox.getRoutingKey(), outbox.getPayload());
        outboxStatusUpdater.delete(outbox.getOutboxId());
    }
}
