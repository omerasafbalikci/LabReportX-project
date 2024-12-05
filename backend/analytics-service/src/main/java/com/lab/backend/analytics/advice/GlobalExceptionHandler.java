package com.lab.backend.analytics.advice;

import com.lab.backend.analytics.utilities.exceptions.EmailSendingFailedException;
import com.lab.backend.analytics.utilities.exceptions.FailedGenerateChartException;
import com.lab.backend.analytics.utilities.exceptions.InvalidEmailFormatException;
import com.lab.backend.analytics.utilities.exceptions.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * GlobalExceptionHandler handles exceptions globally across the application.
 * It intercepts specific exceptions and returns standardized error responses.
 *
 * @author Ömer Asaf BALIKÇI
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles the EmailSendingFailedException.
     * Occurs when the system fails to send an email.
     *
     * @param exception the thrown EmailSendingFailedException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and INTERNAL_SERVER_ERROR (500) status code
     */
    @ExceptionHandler(EmailSendingFailedException.class)
    public ResponseEntity<Object> handleEmailSendingFailedException(EmailSendingFailedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles the FailedGenerateChartException.
     * Occurs when the system fails to generate a chart.
     *
     * @param exception the thrown FailedGenerateChartException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and INTERNAL_SERVER_ERROR (500) status code
     */
    @ExceptionHandler(FailedGenerateChartException.class)
    public ResponseEntity<Object> handleFailedGenerateChartException(FailedGenerateChartException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles the InvalidEmailFormatException.
     * Occurs when an email is provided in an invalid format.
     *
     * @param exception the thrown InvalidEmailFormatException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and BAD_REQUEST (400) status code
     */
    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<Object> handleInvalidEmailFormatException(InvalidEmailFormatException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles the ResponseStatusException.
     * Occurs when a response status exception is thrown.
     *
     * @param exception the thrown ResponseStatusException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and the status code provided in the exception
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
