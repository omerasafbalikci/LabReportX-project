package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when the provided password does not meet the required criteria.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
