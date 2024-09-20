package com.lab.backend.gateway.utilities.exceptions;

public class MissingRolesException extends RuntimeException {
    public MissingRolesException(String message) {
        super(message);
    }
}
