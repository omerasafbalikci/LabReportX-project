package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when a user has no roles assigned.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class NoRolesException extends RuntimeException {
    public NoRolesException(String message) {
        super(message);
    }
}
