package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the token provided is invalid or malformed.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
