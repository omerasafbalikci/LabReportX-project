package com.lab.backend.analytics.controller;

import com.lab.backend.analytics.service.abstracts.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Log4j2
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @PostMapping("/patient-stats")
    public ResponseEntity<byte[]> getPatientChart() {
        byte[] chartImage = this.analyticsService.getPatientChart();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/png");
        headers.add("Content-Disposition", "inline; filename=patient-stats.png");
        return new ResponseEntity<>(chartImage, headers, HttpStatus.OK);
    }
}
