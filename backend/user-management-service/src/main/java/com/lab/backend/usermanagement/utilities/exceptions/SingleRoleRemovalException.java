package com.lab.backend.usermanagement.utilities.exceptions;

/**
 * Custom exception class to handle cases where there is an attempt to remove the last role of a user.
 * Thrown when a user is left without any roles after the operation.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class SingleRoleRemovalException extends RuntimeException {
    public SingleRoleRemovalException(String message) {
        super(message);
    }
}
