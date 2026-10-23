package bkrc.chaptersofu.application.required.outbox;

import bkrc.chaptersofu.application.required.event.Event;
import bkrc.chaptersofu.application.required.event.EventPayload;
import bkrc.chaptersofu.application.required.event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventRecorder {
    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public Outbox record(EventType eventType, String exchange, String routingKey, EventPayload payload) {
        Outbox outbox = outboxRepository.save(Outbox.of(
                eventType, exchange, routingKey, Event.of(eventType, payload).toJson()));
        eventPublisher.publishEvent(OutboxEvent.of(outbox));
        return outbox;
    }
}
