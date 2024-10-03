package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when an email retrieved from the system is null or empty.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class EmailNullException extends RuntimeException {
    public EmailNullException(String message) {
        super(message);
    }
}
