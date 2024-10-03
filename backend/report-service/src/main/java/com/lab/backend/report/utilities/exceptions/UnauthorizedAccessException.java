package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when a user attempts to access a resource they are not authorized to access.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
