package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.utilities.exceptions.ResponseStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceImplTest {
    @Mock
    private AnalyticsConsumer analyticsConsumer;
    @Mock
    private ChartService chartService;
    @Mock
    private MailService mailService;
    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    public void testGetPatientChart_Success() {
        // Arrange
        WeeklyStats mockStats = new WeeklyStats();
        byte[] mockChart = new byte[]{1, 2, 3, 4};
        when(this.analyticsConsumer.getCachedWeeklyPatientStats()).thenReturn(mockStats);
        when(this.chartService.generateChart(any(), any(), any(), any(), eq(mockStats))).thenReturn(mockChart);

        // Act
        byte[] result = this.analyticsService.getPatientChart();

        // Assert
        assertNotNull(result);
        assertArrayEquals(mockChart, result);
        verify(this.analyticsConsumer, times(1)).getCachedWeeklyPatientStats();
        verify(this.chartService, times(1)).generateChart(any(), any(), any(), any(), eq(mockStats));
    }

    @Test
    public void testGetPatientChart_NoStatsAvailable() {
        // Arrange
        when(this.analyticsConsumer.getCachedWeeklyPatientStats()).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> this.analyticsService.getPatientChart());
        assertEquals("No weekly patient stats available", exception.getMessage());
        verify(this.analyticsConsumer, times(1)).getCachedWeeklyPatientStats();
        verifyNoInteractions(this.chartService);
    }

    @Test
    public void testGetReportChart_Success() {
        // Arrange
        WeeklyStats mockStats = new WeeklyStats();
        byte[] mockChart = new byte[]{5, 6, 7, 8};
        when(this.analyticsConsumer.getCachedWeeklyReportStats()).thenReturn(mockStats);
        when(this.chartService.generateChart(any(), any(), any(), any(), eq(mockStats))).thenReturn(mockChart);

        // Act
        byte[] result = this.analyticsService.getReportChart();

        // Assert
        assertNotNull(result);
        assertArrayEquals(mockChart, result);
        verify(this.analyticsConsumer, times(1)).getCachedWeeklyReportStats();
        verify(this.chartService, times(1)).generateChart(any(), any(), any(), any(), eq(mockStats));
    }

    @Test
    public void testGetReportChart_NoStatsAvailable() {
        // Arrange
        when(this.analyticsConsumer.getCachedWeeklyReportStats()).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> this.analyticsService.getReportChart());
        assertEquals("No weekly report stats available", exception.getMessage());
        verify(this.analyticsConsumer, times(1)).getCachedWeeklyReportStats();
        verifyNoInteractions(this.chartService);
    }

    @Test
    public void testSendPatientSatisfactionSurvey() {
        // Arrange
        String email = "test@example.com";
        String subject = "Patient Satisfaction Survey";
        String text = """
                Dear patient,

                We value your feedback. Please take a moment to fill out our patient satisfaction survey by clicking the link below:

                https://forms.gle/tntLg4g1Wrrs2oSQA

                Your input is important to us.

                Best regards,
                Sevda Hospital""";

        // Act
        this.analyticsService.sendPatientSatisfactionSurvey(email);

        // Assert
        verify(this.mailService, times(1)).sendEmail(email, subject, text);
    }
}
