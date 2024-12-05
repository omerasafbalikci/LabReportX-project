package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.service.abstracts.AnalyticsService;
import com.lab.backend.analytics.utilities.exceptions.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AnalyticsServiceImpl implements AnalyticsService {
    private final AnalyticsConsumer analyticsConsumer;
    private final ChartService chartService;
    private final MailService mailService;

    @Override
    public byte[] getPatientChart() {
        WeeklyStats weeklyStats = this.analyticsConsumer.getCachedWeeklyPatientStats();
        if (weeklyStats == null) {
            throw new ResponseStatusException("No weekly patient stats available");
        }
        return this.chartService.generateChart(weeklyStats);
    }

    @Override
    public byte[] getReportChart() {
        WeeklyStats weeklyStats = this.analyticsConsumer.getCachedWeeklyReportStats();
        if (weeklyStats == null) {
            throw new ResponseStatusException("No weekly report stats available");
        }
        return this.chartService.generateChart(weeklyStats);
    }

    @KafkaListener(topics = "patient-email-topic", groupId = "patient-email")
    public void sendPatientSatisfactionSurvey(String email) {
        log.trace("Received email for survey: {}", email);
        String subject = "Patient Satisfaction Survey";
        String text = "Dear patient,\n\nWe value your feedback. Please take a moment to fill out our patient satisfaction survey by clicking the link below:\n\n"
                + "https://forms.gle/tntLg4g1Wrrs2oSQA\n\nYour input is important to us.\n\nBest regards,\nSevda Hospital";

        this.mailService.sendEmail(email, subject, text);
        log.info("Survey email sent to: {}", email);
    }
}
