package com.bkrc.bkrcv3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync  // 비동기 활성화
public class AsyncConfig {
    @Bean(name = "aladinTaskExecutor")
    public ThreadPoolTaskExecutor aladinTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);      // 기본 스레드 수
        executor.setMaxPoolSize(32);      // 최대 스레드 수
        executor.setQueueCapacity(100);   // 대기 큐 크기
        executor.setThreadNamePrefix("aladin-async-");  // 로그 식별용
        executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 작업 완료 대기

        // CallerRunsPolicy — 큐 꽉 차도 데이터 유실 없음
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
