package com.lab.backend.report.config;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up Kafka producer properties and beans.
 * This class defines the configuration for Kafka producer, including serialization
 * and optimization settings for message delivery.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Configuration
@Log4j2
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates a {@link KafkaTemplate} bean for sending messages to Kafka topics.
     *
     * @return a {@link KafkaTemplate} configured with the producer factory
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Creates a {@link ProducerFactory} bean to configure Kafka producer properties.
     * The producer is set up with serialization and delivery options such as batching
     * and idempotence.
     *
     * @return a {@link ProducerFactory} configured with the necessary properties
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.TYPE_MAPPINGS, "weeklyStats:com.lab.backend.report.dto.requests.WeeklyStats");

        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20);

        logProducerConfiguration(configProps);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Logs the configuration properties of the Kafka producer at the trace level.
     *
     * @param configProps a map containing Kafka producer configuration properties
     */
    private void logProducerConfiguration(Map<String, Object> configProps) {
        configProps.forEach((key, value) -> log.trace("Kafka Producer Config - {}: {}", key, value));
    }
}
