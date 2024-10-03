package com.lab.backend.report.advice;

import lombok.Data;

/**
 * This class represents a validation error that occurs during the validation process.
 * It holds information about the object that failed validation, the specific field,
 * the rejected value, and an error message describing the issue.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
public class ValidationError {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;

    public ValidationError(String object, String field, Object rejectedValue, String message) {
        this.object = object;
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }
}
