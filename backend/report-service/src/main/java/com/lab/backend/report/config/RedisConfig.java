package com.lab.backend.report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * Configuration class for setting up Redis.
 * This class is responsible for configuring the Redis connection and repositories.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Configuration
@EnableRedisRepositories
public class RedisConfig {
    /**
     * Creates a RedisTemplate bean for interacting with Redis.
     *
     * @param redisConnectionFactory the connection factory used to connect to Redis
     * @return a configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
