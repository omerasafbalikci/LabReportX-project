package com.lab.backend.auth.utilities.exceptions;

public class EmailSendingFailedException extends RuntimeException {
    public EmailSendingFailedException(String message) {
        super(message);
    }
}
