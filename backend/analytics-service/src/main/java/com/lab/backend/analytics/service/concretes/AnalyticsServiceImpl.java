package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.service.abstracts.AnalyticsService;
import com.lab.backend.analytics.utilities.exceptions.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service implementation for handling analytics-related operations.
 * Provides methods for generating charts and sending emails.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class AnalyticsServiceImpl implements AnalyticsService {
    private final AnalyticsConsumer analyticsConsumer;
    private final ChartService chartService;
    private final MailService mailService;

    /**
     * Generates a chart for patient statistics using cached data.
     *
     * @return a byte array representing the chart image
     * @throws ResponseStatusException if no patient statistics are available in the cache
     */
    @Override
    public byte[] getPatientChart() {
        log.trace("Entering getPatientChart method in AnalyticsServiceImpl");
        WeeklyStats weeklyStats = this.analyticsConsumer.getCachedWeeklyPatientStats();
        if (weeklyStats == null) {
            log.error("No weekly patient stats available");
            throw new ResponseStatusException("No weekly patient stats available");
        }
        log.trace("Exiting getPatientChart method in AnalyticsServiceImpl");
        return this.chartService.generateChart(weeklyStats);
    }

    /**
     * Generates a chart for report statistics using cached data.
     *
     * @return a byte array representing the chart image
     * @throws ResponseStatusException if no report statistics are available in the cache
     */
    @Override
    public byte[] getReportChart() {
        log.trace("Entering getReportChart method in AnalyticsServiceImpl");
        WeeklyStats weeklyStats = this.analyticsConsumer.getCachedWeeklyReportStats();
        if (weeklyStats == null) {
            log.error("No weekly report stats available");
            throw new ResponseStatusException("No weekly report stats available");
        }
        log.trace("Exiting getReportChart method in AnalyticsServiceImpl");
        return this.chartService.generateChart(weeklyStats);
    }

    /**
     * Sends a patient satisfaction survey email to the specified email address.
     * This method is triggered by a Kafka message from the "patient-email-topic".
     *
     * @param email the recipient's email address
     */
    @KafkaListener(topics = "patient-email-topic", groupId = "patient-email")
    public void sendPatientSatisfactionSurvey(String email) {
        log.trace("Entering sendPatientSatisfactionSurvey method with email: {}", email);
        String subject = "Patient Satisfaction Survey";
        String text = "Dear patient,\n\nWe value your feedback. Please take a moment to fill out our patient satisfaction survey by clicking the link below:\n\n"
                + "https://forms.gle/tntLg4g1Wrrs2oSQA\n\nYour input is important to us.\n\nBest regards,\nSevda Hospital";

        this.mailService.sendEmail(email, subject, text);
        log.info("Survey email sent to: {}", email);
        log.trace("Exiting sendPatientSatisfactionSurvey method in AnalyticsServiceImpl");
    }
}
