package com.lab.backend.gateway.utilities.exceptions;

/**
 * Custom exception thrown when the authentication service is unavailable.
 * This exception is used as a fallback response when the Gateway cannot reach the auth service.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class AuthServiceUnavailableException extends RuntimeException {
    public AuthServiceUnavailableException(String message) {
        super(message);
    }
}
