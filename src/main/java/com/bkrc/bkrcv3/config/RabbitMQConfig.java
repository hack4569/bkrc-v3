package com.bkrc.bkrcv3.config;

import com.bkrc.bkrcv3.common.event.EventType;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue 선언 (durable=true: 서버 재시작 후에도 큐 유지)
    @Bean
    public Queue memberJoinQueue() {
        return new Queue(EventType.Queue.MEMBER_JOIN, true);
    }

    @Bean
    public Queue memberModifyQueue() {
        return new Queue(EventType.Queue.MEMBER_MODIFY, true);
    }

    // Exchange 선언 (Topic 방식 — 라우팅 키 패턴 매칭)
    @Bean
    public TopicExchange memberExchange() {
        return new TopicExchange(EventType.EXCHANGE);
    }

    // Exchange → Queue 바인딩
    @Bean
    public Binding memberJoinBinding() {
        return BindingBuilder
                .bind(memberJoinQueue())
                .to(memberExchange())
                .with(EventType.Queue.MEMBER_JOIN);
    }

    @Bean
    public Binding memberModifyBinding() {
        return BindingBuilder
                .bind(memberModifyQueue())
                .to(memberExchange())
                .with(EventType.Queue.MEMBER_MODIFY);
    }

    // JSON 직렬화 설정
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}