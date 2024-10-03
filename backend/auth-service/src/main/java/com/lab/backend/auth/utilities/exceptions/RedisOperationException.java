package com.lab.backend.auth.utilities.exceptions;

/**
 * Exception thrown when a Redis operation fails.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class RedisOperationException extends Exception {
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
