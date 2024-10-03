package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when a user already exists in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
