package com.lab.backend.usermanagement.utilities.exceptions;

/**
 * Custom exception class to handle RabbitMQ related errors.
 * Thrown when there is an issue with RabbitMQ operations.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class RabbitMQException extends RuntimeException {
    public RabbitMQException(String message, Throwable cause) {
        super(message, cause);
    }
}
