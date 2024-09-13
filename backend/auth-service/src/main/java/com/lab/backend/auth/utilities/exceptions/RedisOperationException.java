package com.lab.backend.auth.utilities.exceptions;

public class RedisOperationException extends Exception {
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
