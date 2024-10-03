package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when the extraction of the username fails.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class UsernameExtractionException extends Exception {
    public UsernameExtractionException(String message) {
        super(message);
    }
}
