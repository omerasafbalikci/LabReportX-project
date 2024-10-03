package com.lab.backend.usermanagement.advice;

import com.lab.backend.usermanagement.utilities.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Global exception handler for handling application-wide exceptions in a Spring Boot application.
 * Provides custom responses for different types of exceptions.
 * This class extends {@link ResponseEntityExceptionHandler} and uses {@link ControllerAdvice}
 * to apply these exception handling methods across the whole application.
 *
 * @author Ömer Asaf BALIKÇI
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles {@link UserNotFoundException} exceptions.
     *
     * @param exception The thrown {@link UserNotFoundException}.
     * @param request   The current {@link HttpServletRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a NOT_FOUND status.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link UserAlreadyExistsException} exceptions.
     *
     * @param exception The thrown {@link UserAlreadyExistsException}.
     * @param request   The current {@link HttpServletRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a CONFLICT status.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link SingleRoleRemovalException} exceptions.
     *
     * @param exception The thrown {@link SingleRoleRemovalException}.
     * @param request   The current {@link HttpServletRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a BAD_REQUEST status.
     */
    @ExceptionHandler(SingleRoleRemovalException.class)
    public ResponseEntity<Object> handleSingleRoleRemovalException(SingleRoleRemovalException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link RoleNotFoundException} exceptions.
     *
     * @param exception The thrown {@link RoleNotFoundException}.
     * @param request   The current {@link HttpServletRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a BAD_REQUEST status.
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Object> handleRoleNotFoundException(RoleNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link RoleAlreadyExistsException} exceptions.
     *
     * @param exception The thrown {@link RoleAlreadyExistsException}.
     * @param request   The current {@link HttpServletRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a CONFLICT status.
     */
    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<Object> handleRoleAlreadyExistsException(RoleAlreadyExistsException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link RabbitMQException} exceptions.
     *
     * @param exception The thrown {@link RabbitMQException}.
     * @param request   The current {@link HttpServletRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with an INTERNAL_SERVER_ERROR status.
     */
    @ExceptionHandler(RabbitMQException.class)
    public ResponseEntity<Object> handleRabbitMQException(RabbitMQException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} which occurs when method argument validation fails.
     *
     * @param exception  The thrown {@link MethodArgumentNotValidException}.
     * @param headers    The headers to be sent with the response.
     * @param statusCode The status code of the response.
     * @param request    The current {@link WebRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a BAD_REQUEST status,
     * along with validation errors.
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException exception,
                                                               @NonNull HttpHeaders headers,
                                                               @NonNull HttpStatusCode statusCode,
                                                               @NonNull WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.addValidationError(fieldErrors);
        errorResponse.setPath(servletWebRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link HttpMessageNotReadableException} which occurs when the incoming JSON payload is malformed.
     *
     * @param exception  The thrown {@link HttpMessageNotReadableException}.
     * @param headers    The headers to be sent with the response.
     * @param statusCode The status code of the response.
     * @param request    The current {@link WebRequest} which caused the exception.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with a BAD_REQUEST status,
     * describing the issue with the JSON payload.
     */
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException exception,
                                                               @NonNull HttpHeaders headers,
                                                               @NonNull HttpStatusCode statusCode,
                                                               @NonNull WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Malformed Json Request", exception);
        errorResponse.setPath(servletWebRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
