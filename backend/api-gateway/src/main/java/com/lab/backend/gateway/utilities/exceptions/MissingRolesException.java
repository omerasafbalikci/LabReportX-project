package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when no roles are found in a token during role-based authorization checks.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class MissingRolesException extends RuntimeException {
    public MissingRolesException(String message) {
        super(message);
    }
}
