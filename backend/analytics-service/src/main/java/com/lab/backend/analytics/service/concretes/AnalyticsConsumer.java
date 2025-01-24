package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * The AnalyticsConsumer class is responsible for listening to Kafka topics and caching the weekly statistics for patients and reports.
 * It listens for messages on specific topics, processes them, and stores the data in a cache.
 * This class is a Spring component and utilizes Kafka listeners for message consumption and caching for better performance.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@RequiredArgsConstructor
@Log4j2
public class AnalyticsConsumer {
    private final CacheManager cacheManager;
    private static final String WEEKLY_STATS_CACHE = "weeklyStatsCache";

    /**
     * Listens to the Kafka topic "patient-stats" and caches the weekly patient statistics.
     * This method is triggered when a message is received on the "patient-stats" topic,
     * and the data is cached under the key "patient-latest".
     *
     * @param weeklyStats the WeeklyStats object received from Kafka
     */
    @KafkaListener(topics = "patient-stats", groupId = "patients")
    public void cacheWeeklyPatientStats(WeeklyStats weeklyStats) {
        Objects.requireNonNull(this.cacheManager.getCache(WEEKLY_STATS_CACHE)).put("patient-latest", weeklyStats);
        log.trace("Patient stats successfully cached.");
    }

    /**
     * Listens to the Kafka topic "report-stats" and caches the weekly report statistics.
     * This method is triggered when a message is received on the "report-stats" topic,
     * and the data is cached under the key "report-latest".
     *
     * @param weeklyStats the WeeklyStats object received from Kafka
     */
    @KafkaListener(topics = "report-stats", groupId = "reports")
    public void cacheWeeklyReportStats(WeeklyStats weeklyStats) {
        Objects.requireNonNull(this.cacheManager.getCache(WEEKLY_STATS_CACHE)).put("report-latest", weeklyStats);
        log.trace("Report stats successfully cached.");
    }

    /**
     * Retrieves the cached weekly patient statistics.
     * This method retrieves the "patient-latest" entry from the cache.
     *
     * @return the cached WeeklyStats object for patient statistics
     */
    public WeeklyStats getCachedWeeklyPatientStats() {
        return Objects.requireNonNull(this.cacheManager.getCache(WEEKLY_STATS_CACHE)).get("patient-latest", WeeklyStats.class);
    }

    /**
     * Retrieves the cached weekly report statistics.
     * This method retrieves the "report-latest" entry from the cache.
     *
     * @return the cached WeeklyStats object for report statistics
     */
    public WeeklyStats getCachedWeeklyReportStats() {
        return Objects.requireNonNull(this.cacheManager.getCache(WEEKLY_STATS_CACHE)).get("report-latest", WeeklyStats.class);
    }
}
