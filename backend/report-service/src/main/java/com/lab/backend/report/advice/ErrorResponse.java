package com.lab.backend.report.advice;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String debugMessage;
    private String path;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(HttpStatus status, String message) {
        this();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
    }

    public ErrorResponse(HttpStatus status, String message, Exception exception) {
        this();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.debugMessage = exception.getLocalizedMessage();
    }

    public void addValidationError(List<FieldError> errors) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        for (FieldError error : errors) {
            ValidationError validationError = new ValidationError(
                    error.getObjectName(),
                    error.getField(),
                    error.getRejectedValue(),
                    error.getDefaultMessage()
            );
            validationErrors.add(validationError);
        }
    }
}
