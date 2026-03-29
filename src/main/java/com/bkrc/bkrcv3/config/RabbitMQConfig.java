package com.bkrc.bkrcv3.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String JOIN_QUEUE = "joinQueue";
    public static final String MODIFY_QUEUE = "modifyQueue";
    public static final String LIKE_QUEUE = "likeQueue";

    public static final String JOIN_ROUTING_KEY = "member.join";
    public static final String MODIFY_ROUTING_KEY = "member.modify";
    public static final String LIKE_ROUTING_KEY = "hotbook.like";

    public static final String NOTIFICATION_DIRECT_EXCHANGE = "notificationExchange";
    public static final String HOTBOOK_DIRECT_EXCHANGE = "hotbookExchange";

    public static final String DLQ = "deadLetterQueue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";
    public static final String DEAD_DIRECT_EXCHANGE = "deadLetterExchange";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_DIRECT_EXCHANGE);
    }

    @Bean
    public DirectExchange hotBookExchange() {
        return new DirectExchange(HOTBOOK_DIRECT_EXCHANGE);
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
                .withArgument("x-dead-letter-exchange", DEAD_DIRECT_EXCHANGE) // DLX 설정
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY) // DLQ로 이동할 라우팅 키 설정
                .build();
    }

    @Bean
    public Queue modifyQueue() {
        return QueueBuilder.durable(MODIFY_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_DIRECT_EXCHANGE) // DLX 설정
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY) // DLQ로 이동할 라우팅 키 설정
                .build();
    }

    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(LIKE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_DIRECT_EXCHANGE) // DLX 설정
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

    @Bean
    public Binding likeQueueBinding() {
        return BindingBuilder.bind(likeQueue()).to(hotBookExchange()).with(LIKE_ROUTING_KEY);
    }

    // Dead Letter Queue와 Dead Letter Exchange 바인딩
    @Bean
    public Binding deadLetterQueueBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_ROUTING_KEY);
    }
}
