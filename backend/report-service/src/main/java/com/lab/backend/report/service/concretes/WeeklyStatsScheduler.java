package com.lab.backend.report.service.concretes;

import com.lab.backend.report.service.abstracts.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler component for triggering weekly report statistics task.
 * This class is responsible for scheduling the task of sending weekly report statistics
 * at regular intervals.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@EnableScheduling
@RequiredArgsConstructor
@Log4j2
public class WeeklyStatsScheduler {
    private final ReportService reportService;

    /**
     * Scheduled task to generate and send weekly report statistics every 30 minutes.
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void scheduleWeeklyStatsTask() {
        log.trace("Starting scheduleWeeklyStatsTask in WeeklyStatsScheduler.");
        this.reportService.sendWeeklyReportStats();
        log.trace("Completed scheduleWeeklyStatsTask in WeeklyStatsScheduler.");
    }
}
