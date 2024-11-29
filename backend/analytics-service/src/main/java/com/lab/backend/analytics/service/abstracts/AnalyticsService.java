package com.lab.backend.analytics.service.abstracts;

public interface AnalyticsService {
    byte[] getPatientChart();

    byte[] getReportChart();

    byte[] getTechnicianReportsChart();

    void sendPatientSatisfactionSurvey();
}
