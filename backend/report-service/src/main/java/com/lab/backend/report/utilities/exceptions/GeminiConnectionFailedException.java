package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when there is a failure connecting to the Gemini system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class GeminiConnectionFailedException extends RuntimeException {
    public GeminiConnectionFailedException(String message) {
        super(message);
    }
}
