package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when a report is not found in the system.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(String message) {
        super(message);
    }
}
