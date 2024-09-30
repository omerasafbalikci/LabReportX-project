package com.lab.backend.report.utilities.exceptions;

public class GeminiConnectionFailedException extends RuntimeException {
    public GeminiConnectionFailedException(String message) {
        super(message);
    }
}
