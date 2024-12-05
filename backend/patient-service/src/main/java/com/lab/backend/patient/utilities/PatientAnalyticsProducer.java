package com.lab.backend.patient.utilities;

import com.lab.backend.patient.dto.requests.WeeklyStats;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer component for publishing patient-related analytics data.
 * Handles sending statistics and email notifications to specific Kafka topics.
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

    /**
     * Sends an email address to the specified Kafka topic for survey or other notifications.
     *
     * @param topic the name of the Kafka topic to publish to
     * @param email the email address to send
     */
    public void sendEmail(String topic, String email) {
        log.trace("Preparing to send email to topic: {}", topic);
        this.kafkaTemplate.send(topic, email);
    }
}
