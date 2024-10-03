package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when the provided token is invalid.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
