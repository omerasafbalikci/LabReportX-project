package com.lab.backend.analytics.utilities.exceptions;

public class ResponseStatusException extends RuntimeException {
    public ResponseStatusException(String message) {
        super(message);
    }
}
