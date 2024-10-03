package com.lab.backend.patient.utilities.exceptions;

/**
 * Exception thrown when a camera fails to open.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class CameraNotOpenedException extends RuntimeException {
    public CameraNotOpenedException(String message) {
        super(message);
    }
}
