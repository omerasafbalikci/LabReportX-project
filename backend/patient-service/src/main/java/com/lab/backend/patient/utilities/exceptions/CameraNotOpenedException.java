package com.lab.backend.patient.utilities.exceptions;

public class CameraNotOpenedException extends RuntimeException {
    public CameraNotOpenedException(String message) {
        super(message);
    }
}
