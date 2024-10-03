package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when the provided password is incorrect.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException(String message) {
        super(message);
    }
}
