package com.lab.backend.analytics.controller;

import com.lab.backend.analytics.service.abstracts.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsControllerTest {
    @Mock
    private AnalyticsService analyticsService;
    @InjectMocks
    private AnalyticsController analyticsController;

    @Test
    public void testGetPatientChart() {
        // Arrange
        byte[] dummyImage = new byte[]{1, 2, 3, 4};
        when(this.analyticsService.getPatientChart()).thenReturn(dummyImage);

        // Act
        ResponseEntity<byte[]> response = this.analyticsController.getPatientChart();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("image/png", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("inline; filename=patient-stats.png", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(dummyImage, response.getBody());

        verify(this.analyticsService, times(1)).getPatientChart();
    }

    @Test
    public void testGetReportChart() {
        // Arrange
        byte[] dummyImage = new byte[]{5, 6, 7, 8};
        when(this.analyticsService.getReportChart()).thenReturn(dummyImage);

        // Act
        ResponseEntity<byte[]> response = this.analyticsController.getReportChart();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("image/png", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("inline; filename=report-stats.png", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(dummyImage, response.getBody());

        verify(this.analyticsService, times(1)).getReportChart();
    }
}
