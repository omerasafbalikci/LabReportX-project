package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.service.abstracts.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler component for periodically generating and sending weekly patient registration statistics.
 *
 * <p>This scheduler uses a cron expression to trigger the task at specified intervals. It calls the
 * {@link PatientService#sendWeeklyPatientRegistrationStats()} method to generate the statistics and
 * send them to the configured Kafka topic.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@EnableScheduling
@RequiredArgsConstructor
@Log4j2
public class WeeklyStatsScheduler {
    private final PatientService patientService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void scheduleWeeklyStatsTask() {
        log.trace("Starting scheduleWeeklyStatsTask in WeeklyStatsScheduler.");
        this.patientService.sendWeeklyPatientRegistrationStats();
        log.trace("Completed scheduleWeeklyStatsTask in WeeklyStatsScheduler.");
    }
}
