package com.lab.backend.gateway.utilities.exceptions;

public class InsufficientRolesException extends RuntimeException {
    public InsufficientRolesException(String message) {
        super(message);
    }
}
