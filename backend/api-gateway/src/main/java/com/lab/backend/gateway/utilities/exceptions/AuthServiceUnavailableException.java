package com.lab.backend.gateway.utilities.exceptions;

public class AuthServiceUnavailableException extends RuntimeException {
    public AuthServiceUnavailableException(String message) {
        super(message);
    }
}
