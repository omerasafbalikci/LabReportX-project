package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when an unexpected error occurs in the application.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UnexpectedException extends RuntimeException {
    public UnexpectedException(String message) {
        super(message);
    }
}
