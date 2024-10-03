package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when a token cannot be found during authentication checks.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
