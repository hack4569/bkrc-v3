package com.bkrc.bkrcv3.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeadLetterRetry {

    private final RabbitTemplate rabbitTemplate;

    public NotificationDeadLetterRetry(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    //@RabbitListener(queues = RabbitMQConfig.DLQ)
    public void processDlqMessage(String failedMessage) {
        try {
            System.out.println("processDlqMessage");
        } catch (Exception e) {
            System.err.println("Error processDlqMessage : " + e);
        }
    }
}
