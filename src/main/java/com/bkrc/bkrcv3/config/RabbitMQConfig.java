package com.bkrc.bkrcv3.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String JOIN_QUEUE = "joinQueue";
    public static final String MODIFY_QUEUE = "modifyQueue";
    public static final String JOIN_ROUTING_KEY = "member.join";
    public static final String MODIFY_ROUTING_KEY = "member.modify";

    public static final String DLQ = "deadLetterQueue";
    public static final String NOTIFICATION_DIRECT_EXCHANGE = "notificationExchange";
    public static final String NOTIFICATION_DIRECT_DLX = "deadLetterExchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_DIRECT_EXCHANGE);
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(NOTIFICATION_DIRECT_DLX);
    }

    // 원래 큐 설정
    @Bean
    public Queue joinQueue() {
        return QueueBuilder.durable(JOIN_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DIRECT_DLX) // DLX 설정
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY) // DLQ로 이동할 라우팅 키 설정
                .build();
    }

    @Bean
    public Queue modifyQueue() {
        return QueueBuilder.durable(MODIFY_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DIRECT_DLX) // DLX 설정
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY) // DLQ로 이동할 라우팅 키 설정
                .build();
    }

    // Dead Letter Queue 설정
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    // 원래 큐와 Exchange 바인딩
    @Bean
    public Binding joinQueueBinding() {
        return BindingBuilder.bind(joinQueue()).to(notificationExchange()).with(JOIN_ROUTING_KEY);
    }

    @Bean
    public Binding modifyQueueBinding() {
        return BindingBuilder.bind(modifyQueue()).to(notificationExchange()).with(MODIFY_ROUTING_KEY);
    }

    // Dead Letter Queue와 Dead Letter Exchange 바인딩
    @Bean
    public Binding deadLetterQueueBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_ROUTING_KEY);
    }
}
