package com.lab.backend.patient.utilities.exceptions;

/**
 * Exception thrown when an unexpected error occurs.
 */

public class UnexpectedException extends RuntimeException {
    public UnexpectedException(String message) {
        super(message);
    }
}
