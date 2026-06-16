package com.heang.koriaibackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Thread pool backing {@code @Async("pushExecutor")} so external push delivery
 * never blocks the request thread that wrote the notification.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "pushExecutor")
    Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("push-");
        executor.initialize();
        return executor;
    }
}
