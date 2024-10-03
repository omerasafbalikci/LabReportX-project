package com.lab.backend.report.advice;

import com.lab.backend.report.utilities.exceptions.*;
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
 * Global exception handler for handling various exceptions thrown in the application.
 *
 * @author Ömer Asaf BALIKÇI
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles {@link FileStorageException} and returns a response with the appropriate HTTP status and error message.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Object> handleFileStorageException(FileStorageException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link ReportNotFoundException} and returns a response with a NOT FOUND status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Object> handleReportNotFoundException(ReportNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link UnexpectedException} and returns a response with an INTERNAL SERVER ERROR status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<Object> handleUnexpectedException(UnexpectedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link UnauthorizedAccessException} and returns a response with a FORBIDDEN status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccessException(UnauthorizedAccessException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link EmailNullException} and returns a response with a BAD REQUEST status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(EmailNullException.class)
    public ResponseEntity<Object> handleEmailNullException(EmailNullException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link EmailSendingFailedException} and returns a response with an INTERNAL SERVER ERROR status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(EmailSendingFailedException.class)
    public ResponseEntity<Object> handleEmailSendingFailedException(EmailSendingFailedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link GeminiConnectionFailedException} and returns a response with an INTERNAL SERVER ERROR status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(GeminiConnectionFailedException.class)
    public ResponseEntity<Object> handleGeminiConnectionFailedException(GeminiConnectionFailedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link InvalidEmailFormatException} and returns a response with a BAD REQUEST status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<Object> handleInvalidEmailFormatException(InvalidEmailFormatException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link InvalidTrIdNumberException} and returns a response with a BAD REQUEST status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(InvalidTrIdNumberException.class)
    public ResponseEntity<Object> handleInvalidTrIdNumberException(InvalidTrIdNumberException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link PatientNotFoundException} and returns a response with a NOT FOUND status.
     *
     * @param exception the exception thrown
     * @param request   the current HTTP request
     * @return a ResponseEntity containing the error response
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Object> handlePatientNotFoundException(PatientNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} and returns a response with a BAD REQUEST status,
     * including validation error details.
     *
     * @param exception  the exception thrown
     * @param headers    the HTTP headers
     * @param statusCode the HTTP status code
     * @param request    the current HTTP request
     * @return a ResponseEntity containing the error response
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
     * Handles {@link HttpMessageNotReadableException} and returns a response with a BAD REQUEST status,
     * indicating a malformed JSON request.
     *
     * @param exception  the exception thrown
     * @param headers    the HTTP headers
     * @param statusCode the HTTP status code
     * @param request    the current HTTP request
     * @return a ResponseEntity containing the error response
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
