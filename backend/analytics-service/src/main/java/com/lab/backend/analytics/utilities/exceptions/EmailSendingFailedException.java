package com.lab.backend.analytics.utilities.exceptions;

/**
 * Exception thrown when email sending fails.
 * This exception is used when there is an issue with sending an email, such as connection problems or invalid email configurations.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class EmailSendingFailedException extends RuntimeException {
    public EmailSendingFailedException(String message) {
        super(message);
    }
}
