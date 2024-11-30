package com.lab.backend.patient.utilities;

import com.lab.backend.patient.dto.requests.WeeklyStats;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * A Kafka producer component for sending patient registration statistics to a specified topic.
 * This class is responsible for publishing weekly patient registration stats to Kafka.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@AllArgsConstructor
@Log4j2
public class PatientAnalyticsProducer {
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends weekly patient registration statistics to the specified Kafka topic.
     *
     * @param topic       the name of the Kafka topic to publish to
     * @param weeklyStats the statistics object containing weekly registration data
     */
    public void sendPatientRegistrationStats(String topic, WeeklyStats weeklyStats) {
        log.trace("Preparing to send weekly patient registration stats to topic: {}", topic);
        this.kafkaTemplate.send(topic, weeklyStats);
    }
}
