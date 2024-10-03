package com.lab.backend.patient.advice;

import com.lab.backend.patient.utilities.exceptions.*;
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
 * Global exception handler that handles exceptions thrown across the whole application.
 * Provides centralized exception handling for all controllers and returns appropriate
 * HTTP responses based on the exception type.
 *
 * @author Ömer Asaf BALIKÇI
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles PatientNotFoundException and returns a NOT_FOUND response.
     *
     * @param exception the PatientNotFoundException
     * @param request   the HttpServletRequest
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Object> handlePatientNotFoundException(PatientNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles PatientAlreadyExistsException and returns a CONFLICT response.
     *
     * @param exception the PatientAlreadyExistsException
     * @param request   the HttpServletRequest
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(PatientAlreadyExistsException.class)
    public ResponseEntity<Object> handlePatientAlreadyExistsException(PatientAlreadyExistsException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles CameraNotOpenedException and returns an INTERNAL_SERVER_ERROR response.
     *
     * @param exception the CameraNotOpenedException
     * @param request   the HttpServletRequest
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(CameraNotOpenedException.class)
    public ResponseEntity<Object> handleCameraNotOpenedException(CameraNotOpenedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles UnexpectedException and returns an INTERNAL_SERVER_ERROR response.
     *
     * @param exception the UnexpectedException
     * @param request   the HttpServletRequest
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<Object> handleUnexpectedException(UnexpectedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles InvalidTrIdNumberException and returns a BAD_REQUEST response.
     *
     * @param exception the InvalidTrIdNumberException
     * @param request   the HttpServletRequest
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(InvalidTrIdNumberException.class)
    public ResponseEntity<Object> handleInvalidTrIdNumberException(InvalidTrIdNumberException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation errors in method arguments and returns a BAD_REQUEST response.
     *
     * @param exception  the MethodArgumentNotValidException
     * @param headers    the HttpHeaders
     * @param statusCode the HttpStatusCode
     * @param request    the WebRequest
     * @return ResponseEntity containing the validation error details
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
     * Handles errors related to unreadable HTTP message requests (e.g., malformed JSON).
     * Returns a BAD_REQUEST response.
     *
     * @param exception  the HttpMessageNotReadableException
     * @param headers    the HttpHeaders
     * @param statusCode the HttpStatusCode
     * @param request    the WebRequest
     * @return ResponseEntity containing the error details
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
