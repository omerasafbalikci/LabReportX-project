package com.lab.backend.patient.utilities.exceptions;

/**
 * PatientAlreadyExistsException thrown if patient already exists.
 */

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String message) {
        super(message);
    }
}
