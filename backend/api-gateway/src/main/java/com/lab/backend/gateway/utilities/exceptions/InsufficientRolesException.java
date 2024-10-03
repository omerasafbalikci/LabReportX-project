package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the user does not have sufficient roles to access a resource.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InsufficientRolesException extends RuntimeException {
    public InsufficientRolesException(String message) {
        super(message);
    }
}
