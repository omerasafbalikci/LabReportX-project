package com.lab.backend.gateway.utilities.exceptions;

public class ReportServiceUnavailableException extends RuntimeException {
    public ReportServiceUnavailableException(String message) {
        super(message);
    }
}
