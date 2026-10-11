package bkrc.chaptersofu.adapter.messaging.consumer;

import bkrc.chaptersofu.application.messaging.RabbitMQConfig;
import bkrc.chaptersofu.domain.shared.event.Event;
import bkrc.chaptersofu.application.required.EventPayload;
import bkrc.chaptersofu.application.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {
    private final EmailService emailService;

    @RabbitListener(
        queues = {
                RabbitMQConfig.JOIN_QUEUE,
                RabbitMQConfig.MODIFY_QUEUE,
                RabbitMQConfig.WITHDRAW_QUEUE
        }
    )
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            emailService.handleEvent(event);
        }
    }
}
