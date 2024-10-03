package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the report service is unavailable.
 * This is used for circuit breaker fallbacks in the gateway.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class ReportServiceUnavailableException extends RuntimeException {
    public ReportServiceUnavailableException(String message) {
        super(message);
    }
}
