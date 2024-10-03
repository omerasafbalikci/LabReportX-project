package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when a token cannot be found.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
