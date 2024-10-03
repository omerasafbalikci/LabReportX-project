package com.lab.backend.gateway.utilities.exceptions;

/**
 * Exception thrown when the Authorization header is missing or invalid in a request.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class MissingAuthorizationHeaderException extends RuntimeException {
    public MissingAuthorizationHeaderException(String message) {
        super(message);
    }
}
