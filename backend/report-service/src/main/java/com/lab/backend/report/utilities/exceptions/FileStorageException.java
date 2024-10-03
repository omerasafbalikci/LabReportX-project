package com.lab.backend.report.utilities.exceptions;

/**
 * Exception thrown when there is an issue with file storage operations.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }
}
