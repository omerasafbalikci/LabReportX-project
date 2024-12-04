package com.lab.backend.report.utilities;

import com.lab.backend.report.dto.requests.WeeklyStats;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer class for sending report analytics statistics to Kafka topics.
 * This class is responsible for publishing weekly report statistics to a specified Kafka topic.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@AllArgsConstructor
@Log4j2
public class ReportAnalyticsProducer {
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends weekly report statistics to the specified Kafka topic.
     *
     * @param topic       the name of the Kafka topic to send the message to
     * @param weeklyStats the {@link WeeklyStats} object containing the statistics to be sent
     */
    public void sendReportStats(String topic, WeeklyStats weeklyStats) {
        log.trace("Preparing to send weekly report stats to topic: {}", topic);
        this.kafkaTemplate.send(topic, weeklyStats);
    }
}
