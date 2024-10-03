package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when an invalid email format is detected.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}
