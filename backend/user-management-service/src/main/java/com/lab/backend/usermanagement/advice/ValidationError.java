package com.lab.backend.usermanagement.advice;

import lombok.Data;

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
