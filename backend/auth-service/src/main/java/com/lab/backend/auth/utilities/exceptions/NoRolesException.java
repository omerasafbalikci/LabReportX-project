package com.lab.backend.auth.utilities.exceptions;

public class NoRolesException extends RuntimeException {
    public NoRolesException(String message) {
        super(message);
    }
}
