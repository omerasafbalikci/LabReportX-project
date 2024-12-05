package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the analytics service is unavailable.
 * This exception is used when there is an issue with the analytics service,
 * such as when the service is down or unable to process a request due to an internal error.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class AnalyticsServiceUnavailableException extends RuntimeException {
    public AnalyticsServiceUnavailableException(String message) {
        super(message);
    }
}
