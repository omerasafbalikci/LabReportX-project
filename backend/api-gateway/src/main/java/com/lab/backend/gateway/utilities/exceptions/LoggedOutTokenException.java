package com.lab.backend.gateway.utilities.exceptions;

public class LoggedOutTokenException extends RuntimeException {
    public LoggedOutTokenException(String message) {
        super(message);
    }
}
