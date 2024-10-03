package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when a patient is not found in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
