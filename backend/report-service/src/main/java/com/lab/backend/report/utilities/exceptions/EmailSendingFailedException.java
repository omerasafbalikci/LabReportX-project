package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when sending an email fails.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class EmailSendingFailedException extends RuntimeException {
    public EmailSendingFailedException(String message) {
        super(message);
    }
}
