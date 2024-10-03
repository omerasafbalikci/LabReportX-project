package com.lab.backend.usermanagement.utilities.exceptions;

/**
 * Custom exception class to handle cases where a user already exists.
 * Thrown when trying to create a user that already exists in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
