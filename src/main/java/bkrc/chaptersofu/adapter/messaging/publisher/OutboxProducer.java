package bkrc.chaptersofu.adapter.messaging.publisher;

import bkrc.chaptersofu.application.outbox.Outbox;
import bkrc.chaptersofu.application.outbox.OutboxEvent;
import bkrc.chaptersofu.application.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProducer implements OutboxPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxEvent(OutboxEvent outboxEvent) {
        publish(outboxEvent.getOutbox());
    }

    @Override
    public void publish(Outbox outbox) {
        CorrelationData correlationData = new CorrelationData(
                String.valueOf(outbox.getOutboxId())  // outboxId를 키로
        );

        try {
            rabbitTemplate.convertAndSend(
                    outbox.getExchange(),
                    outbox.getRoutingKey(),
                    outbox.getPayload(),
                    correlationData
            );
        } catch (RuntimeException exception) {
            log.warn("Outbox publish failed - outboxId: {}", outbox.getOutboxId(), exception);
        }
    }

}
