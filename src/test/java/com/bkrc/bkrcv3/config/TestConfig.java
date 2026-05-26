package com.bkrc.bkrcv3.config;

import com.bkrc.bkrcv3.history.HistoryTestFixture;
import com.bkrc.bkrcv3.member.MemberTestFixture;
import net.javacrumbs.shedlock.core.LockProvider;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
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

    @Bean
    @Scope("prototype")
    MemberTestFixture memberTestFixture(Environment environment) {
        return MemberTestFixture.create(environment);
    }

    @Bean
    @Scope("prototype")
    HistoryTestFixture historyTestFixture(Environment environment) {
        return HistoryTestFixture.create(environment);
    }
}