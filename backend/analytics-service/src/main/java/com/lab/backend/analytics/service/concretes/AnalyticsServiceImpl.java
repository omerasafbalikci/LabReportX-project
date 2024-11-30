package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.service.abstracts.AnalyticsService;
import com.lab.backend.analytics.utilities.exceptions.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class AnalyticsServiceImpl implements AnalyticsService {
    private final AnalyticsConsumer analyticsConsumer;
    private final WebClient.Builder webClientBuilder;
    private final String jwt = HttpHeaders.AUTHORIZATION.substring(7);
    private final ChartService chartService;
    private final MailService mailService;

    @Override
    public byte[] getPatientChart() {
        WeeklyStats weeklyStats = analyticsConsumer.getCachedWeeklyStats();
        if (weeklyStats == null) {
            throw new ResponseStatusException("No weekly stats available");
        }
        return this.chartService.generateChart(weeklyStats);
    }

    @Override
    public byte[] getReportChart() {
        return new byte[0];
    }

    @Override
    public byte[] getTechnicianReportsChart() {
        return new byte[0];
    }

    @Override
    public void sendPatientSatisfactionSurvey() {

    }
}
