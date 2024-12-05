package com.lab.backend.analytics.utilities.exceptions;

/**
 * Exception thrown when an email format is invalid.
 * This exception is used when the provided email does not match a valid format, typically during input validation.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}
