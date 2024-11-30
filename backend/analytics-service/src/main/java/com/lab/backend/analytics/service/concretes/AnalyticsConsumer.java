package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Log4j2
public class AnalyticsConsumer {
    private final CacheManager cacheManager;

    @KafkaListener(topics = "patient-stats", groupId = "patients")
    public void cacheWeeklyStats(WeeklyStats weeklyStats) {
        Objects.requireNonNull(this.cacheManager.getCache("weeklyStatsCache")).put("latest", weeklyStats);
        log.trace("cacheWeeklyStats basarili");
    }

    public WeeklyStats getCachedWeeklyStats() {
        return Objects.requireNonNull(this.cacheManager.getCache("weeklyStatsCache")).get("latest", WeeklyStats.class);
    }

}
