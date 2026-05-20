package com.bkrc.bkrcv3.config;

import net.javacrumbs.shedlock.core.LockProvider;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return mock(StringRedisTemplate.class);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

}