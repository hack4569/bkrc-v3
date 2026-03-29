package com.bkrc.bkrcv3.hotbook.consumer;

import com.bkrc.bkrcv3.common.event.Event;
import com.bkrc.bkrcv3.config.RabbitMQConfig;
import com.bkrc.bkrcv3.email.application.EmailService;
import com.bkrc.bkrcv3.hotbook.application.HotBookService;
import com.bkrc.bkrcv3.required.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotBookConsumer {
    private final HotBookService hotBookService;

    @RabbitListener(
            queues = {
                    RabbitMQConfig.LIKE_QUEUE
            }
    )
    public void listen(String message) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            hotBookService.handleEvent(event);
        }
    }
}
