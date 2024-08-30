package com.lab.backend.patient.utilities.exceptions;

/**
 * PatientNotFoundException thrown if patient not found.
 */

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
