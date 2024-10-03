package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the user service is unavailable.
 * This is used for circuit breaker fallbacks in the gateway.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
