package com.lab.backend.gateway.utilities.exceptions;

public class PatientServiceUnavailableException extends RuntimeException {
    public PatientServiceUnavailableException(String message) {
        super(message);
    }
}
