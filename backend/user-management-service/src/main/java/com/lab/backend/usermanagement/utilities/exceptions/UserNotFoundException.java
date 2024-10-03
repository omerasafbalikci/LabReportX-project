package com.lab.backend.usermanagement.utilities.exceptions;

/**
 * Custom exception class to handle cases where a user is not found.
 * Thrown when trying to access a user that does not exist in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
