package com.lab.backend.usermanagement.utilities.exceptions;

public class RabbitMQException extends RuntimeException {
    public RabbitMQException(String message, Throwable cause) {
        super(message, cause);
    }
}
