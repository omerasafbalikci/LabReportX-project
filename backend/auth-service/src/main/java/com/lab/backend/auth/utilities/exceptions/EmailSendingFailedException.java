package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when an email fails to send.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class EmailSendingFailedException extends RuntimeException {
    public EmailSendingFailedException(String message) {
        super(message);
    }
}
