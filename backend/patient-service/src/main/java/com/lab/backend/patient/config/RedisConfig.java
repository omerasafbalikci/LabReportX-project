package com.lab.backend.patient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for setting up Redis with custom serialization settings.
 * This configuration ensures that the RedisTemplate uses String serialization
 * for keys and JDK serialization for values.
 * RedisTemplate is a helper class that simplifies Redis data access, and
 * this class customizes the way the keys and values are serialized for storage in Redis.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Configuration
public class RedisConfig {
    /**
     * Configures and provides a RedisTemplate bean for interacting with Redis.
     * The template uses StringRedisSerializer for serializing the keys as strings
     * and JdkSerializationRedisSerializer for serializing values.
     *
     * @param connectionFactory the RedisConnectionFactory for establishing Redis connections
     * @return a configured RedisTemplate with custom serializers
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        return template;
    }
}
