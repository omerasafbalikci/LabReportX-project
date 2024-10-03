package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when the user's email is not verified.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
