package com.lab.backend.analytics.utilities.exceptions;

/**
 * Exception thrown if the response returned is incorrect.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class ResponseStatusException extends RuntimeException {
    public ResponseStatusException(String message) {
        super(message);
    }
}
