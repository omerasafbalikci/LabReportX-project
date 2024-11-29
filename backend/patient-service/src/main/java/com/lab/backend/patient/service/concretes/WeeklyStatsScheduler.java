package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.service.abstracts.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class WeeklyStatsScheduler {
    private final PatientService patientService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleWeeklyStatsTask() {
        this.patientService.sendWeeklyPatientRegistrationStats();
    }
}
