package com.lab.backend.auth.advice;

import com.lab.backend.auth.utilities.exceptions.*;
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
 * GlobalExceptionHandler handles exceptions globally across the application.
 * It intercepts specific exceptions and returns standardized error responses.
 *
 * @author Ömer Asaf BALIKÇI
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles UserNotFoundException.
     *
     * @param exception the exception thrown when the user is not found
     * @param request   the HttpServletRequest containing details of the request
     * @return a ResponseEntity with a NOT_FOUND status and an error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UsernameExtractionException.
     *
     * @param exception the exception thrown when username extraction fails
     * @param request   the HttpServletRequest containing details of the request
     * @return a ResponseEntity with a BAD_REQUEST status and an error message
     */
    @ExceptionHandler(UsernameExtractionException.class)
    public ResponseEntity<Object> handleUsernameExtractionException(UsernameExtractionException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles UserAlreadyExistsException.
     *
     * @param exception the exception thrown when a user already exists
     * @param request   the HttpServletRequest containing details of the request
     * @return a ResponseEntity with a CONFLICT status and an error message
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles UnexpectedException.
     *
     * @param exception the exception thrown when an unexpected error occurs
     * @param request   the HttpServletRequest containing details of the request
     * @return a ResponseEntity with an INTERNAL_SERVER_ERROR status and an error message
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<Object> handleUnexpectedException(UnexpectedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles InvalidTokenException.
     *
     * @param exception the exception thrown when the token is invalid
     * @param request   the HttpServletRequest containing details of the request
     * @return a ResponseEntity with a BAD_REQUEST status and an error message
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles the RoleNotFoundException.
     * Occurs when the specified role is not found in the system.
     *
     * @param exception the thrown RoleNotFoundException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and NOT_FOUND (404) status code
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Object> handleRoleNotFoundException(RoleNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles the TokenNotFoundException.
     * Occurs when the specified token is not found in the system.
     *
     * @param exception the thrown TokenNotFoundException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and NOT_FOUND (404) status code
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<Object> handleTokenNotFoundException(TokenNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles the RedisOperationException.
     * Occurs when an issue arises during Redis operations.
     *
     * @param exception the thrown RedisOperationException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and INTERNAL_SERVER_ERROR (500) status code
     */
    @ExceptionHandler(RedisOperationException.class)
    public ResponseEntity<Object> handleRedisOperationException(RedisOperationException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles the NoRolesException.
     * Occurs when no roles are found for a user.
     *
     * @param exception the thrown NoRolesException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and BAD_REQUEST (400) status code
     */
    @ExceptionHandler(NoRolesException.class)
    public ResponseEntity<Object> handleNoRolesException(NoRolesException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
     * Handles the IncorrectPasswordException.
     * Occurs when a user provides an incorrect password.
     *
     * @param exception the thrown IncorrectPasswordException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and UNAUTHORIZED (401) status code
     */
    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<Object> handleIncorrectPasswordException(IncorrectPasswordException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

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
     * Handles the EmailNotVerifiedException.
     * Occurs when a user tries to perform an action without verifying their email.
     *
     * @param exception the thrown EmailNotVerifiedException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and FORBIDDEN (403) status code
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Object> handleEmailNotVerifiedException(EmailNotVerifiedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles InvalidPasswordException.
     *
     * @param exception the thrown InvalidPasswordException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and BAD_REQUEST (400) status code
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handleInvalidPasswordException(InvalidPasswordException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles the AuthenticationFailedException.
     * Occurs when authentication fails due to incorrect credentials or token issues.
     *
     * @param exception the thrown AuthenticationFailedException
     * @param request   the HTTP request during which the exception occurred
     * @return ResponseEntity containing the error response and UNAUTHORIZED (401) status code
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Object> handleAuthenticationFailedException(AuthenticationFailedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles validation errors when arguments are not valid.
     *
     * @param exception  the exception thrown when method arguments are invalid
     * @param headers    HTTP headers for the response
     * @param statusCode HTTP status code to return
     * @param request    the WebRequest object containing request details
     * @return a ResponseEntity with a BAD_REQUEST status and validation error details
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
     * Handles malformed JSON requests.
     *
     * @param exception  the exception thrown when the JSON request body is malformed
     * @param headers    HTTP headers for the response
     * @param statusCode HTTP status code to return
     * @param request    the WebRequest object containing request details
     * @return a ResponseEntity with a BAD_REQUEST status and error details
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
