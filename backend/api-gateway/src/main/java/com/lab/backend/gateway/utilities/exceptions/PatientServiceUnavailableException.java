package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the patient service is unavailable.
 * This is used for circuit breaker fallbacks in the gateway.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class PatientServiceUnavailableException extends RuntimeException {
    public PatientServiceUnavailableException(String message) {
        super(message);
    }
}
