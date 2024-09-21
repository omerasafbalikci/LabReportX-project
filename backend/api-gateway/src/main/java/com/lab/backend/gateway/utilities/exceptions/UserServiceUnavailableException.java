package com.lab.backend.gateway.utilities.exceptions;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
