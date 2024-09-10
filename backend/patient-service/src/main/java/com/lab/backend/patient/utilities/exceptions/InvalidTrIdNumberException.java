package com.lab.backend.patient.utilities.exceptions;

public class InvalidTrIdNumberException extends RuntimeException {
    public InvalidTrIdNumberException(String message) {
        super(message);
    }
}
