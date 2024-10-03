package com.lab.backend.usermanagement.utilities.exceptions;

/**
 * Custom exception class to handle cases where a role is not found.
 * Thrown when trying to access a role that does not exist in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
