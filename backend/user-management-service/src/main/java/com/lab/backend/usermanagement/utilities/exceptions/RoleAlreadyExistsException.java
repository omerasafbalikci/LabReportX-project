package com.lab.backend.usermanagement.utilities.exceptions;

/**
 * Custom exception class to handle cases where a role already exists.
 * Thrown when trying to create a role that already exists in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String message) {
        super(message);
    }
}
