package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when the provided email format is invalid.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}
