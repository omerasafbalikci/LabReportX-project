package com.lab.backend.analytics.service.abstracts;

/**
 * Service interface for analytics operations related to generating charts.
 * This interface defines methods for retrieving charts in the form of byte arrays.
 * Implementations of this interface should provide the logic for generating and returning the charts.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface AnalyticsService {
    /**
     * Generates and retrieves the patient statistics chart.
     * The chart is returned as a byte array representing the PNG image.
     *
     * @return a byte array representing the patient statistics chart in PNG format
     */
    byte[] getPatientChart();

    /**
     * Generates and retrieves the report statistics chart.
     * The chart is returned as a byte array representing the PNG image.
     *
     * @return a byte array representing the report statistics chart in PNG format
     */
    byte[] getReportChart();
}
