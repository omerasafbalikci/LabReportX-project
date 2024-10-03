package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when an invalid TR ID number format is detected.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class InvalidTrIdNumberException extends RuntimeException {
    public InvalidTrIdNumberException(String message) {
        super(message);
    }
}
