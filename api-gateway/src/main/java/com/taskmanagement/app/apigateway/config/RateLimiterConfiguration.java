package com.taskmanagement.app.apigateway.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class RateLimiterConfiguration {

    // 20 requests/sec for all general services
    @Bean
    public RateLimiter defaultRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(20)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(0)) // reject immediately, don't queue
                .build();
        return RateLimiterRegistry.of(config).rateLimiter("default");
    }

    // 5 requests/sec for auth endpoints (brute force protection)
    @Bean
    public RateLimiter authRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(0))
                .build();
        return RateLimiterRegistry.of(config).rateLimiter("auth");
    }
}