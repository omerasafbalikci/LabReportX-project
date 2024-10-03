package com.lab.backend.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


/**
 * Resilience4JConfig is a configuration class that customizes the default settings for circuit breakers
 * using Resilience4J in a Spring Cloud Gateway application. It defines circuit breaker and time limiter
 * configurations to handle failures and response time constraints.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Configuration
public class Resilience4JConfig {
    /**
     * Configures the default settings for all circuit breakers in the application using Resilience4J.
     * The configuration includes:
     * - CircuitBreaker:
     * - Failure rate threshold of 50%
     * - Sliding window size of 10 calls
     * - Wait duration in open state of 1 second
     * - TimeLimiter:
     * - Timeout duration of 30 seconds for limiting the time of execution.
     *
     * @return a customizer for {@link ReactiveResilience4JCircuitBreakerFactory} which applies the default configuration.
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slidingWindowSize(10)
                        .waitDurationInOpenState(Duration.ofSeconds(1))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(30))
                        .build())
                .build());
    }
}
