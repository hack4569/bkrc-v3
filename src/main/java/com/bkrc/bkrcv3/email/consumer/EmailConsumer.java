package com.bkrc.bkrcv3.email.consumer;

import com.bkrc.bkrcv3.adapter.payload.MemberJoinEventPayload;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.common.event.EventType;
import com.bkrc.bkrcv3.required.EmailEventHandler;
import com.bkrc.bkrcv3.required.EventPayload;
import com.bkrc.bkrcv3.email.application.EmailService;
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
                RabbitMQConfig.MODIFY_QUEUE
        }
    )
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            emailService.handleEvent(event);
        }
    }
}
