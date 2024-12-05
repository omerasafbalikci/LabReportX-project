package com.lab.backend.analytics.utilities.exceptions;

/**
 * Exception thrown when chart generation fails.
 * This exception is used when there is an issue generating a chart, such as a data processing or rendering failure.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class FailedGenerateChartException extends RuntimeException {
    public FailedGenerateChartException(String message) {
        super(message);
    }
}
