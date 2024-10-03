package com.lab.backend.patient.utilities.exceptions;

/**
 * PatientAlreadyExistsException thrown if patient already exists.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String message) {
        super(message);
    }
}
