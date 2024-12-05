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

/**
 * REST controller for managing analytics.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Log4j2
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    /**
     * Endpoint to generate and retrieve the patient statistics chart as an image.
     * The chart is returned as a PNG image with appropriate HTTP headers for inline display.
     *
     * @return ResponseEntity containing the chart image and HTTP headers with status OK
     */
    @PostMapping("/patient-stats")
    public ResponseEntity<byte[]> getPatientChart() {
        log.trace("Entering getPatientChart method in AnalyticsController class");
        byte[] chartImage = this.analyticsService.getPatientChart();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/png");
        headers.add("Content-Disposition", "inline; filename=patient-stats.png");
        log.trace("Exiting getPatientChart method in AnalyticsController class");
        return new ResponseEntity<>(chartImage, headers, HttpStatus.OK);
    }

    /**
     * Endpoint to generate and retrieve the report statistics chart as an image.
     * The chart is returned as a PNG image with appropriate HTTP headers for inline display.
     *
     * @return ResponseEntity containing the chart image and HTTP headers with status OK
     */
    @PostMapping("/report-stats")
    public ResponseEntity<byte[]> getReportChart() {
        log.trace("Entering getReportChart method in AnalyticsController class");
        byte[] chartImage = this.analyticsService.getReportChart();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/png");
        headers.add("Content-Disposition", "inline; filename=report-stats.png");
        log.trace("Exiting getReportChart method in AnalyticsController class");
        return new ResponseEntity<>(chartImage, headers, HttpStatus.OK);
    }
}
