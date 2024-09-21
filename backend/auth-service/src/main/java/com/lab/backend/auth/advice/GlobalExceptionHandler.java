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

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameExtractionException.class)
    public ResponseEntity<Object> handleUsernameExtractionException(UsernameExtractionException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<Object> handleUnexpectedException(UnexpectedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Object> handleRoleNotFoundException(RoleNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RedisOperationException.class)
    public ResponseEntity<Object> handleRedisOperationException(RedisOperationException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoRolesException.class)
    public ResponseEntity<Object> handleNoRolesException(NoRolesException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<Object> handleInvalidEmailFormatException(InvalidEmailFormatException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<Object> handleIncorrectPasswordException(IncorrectPasswordException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailSendingFailedException.class)
    public ResponseEntity<Object> handleEmailSendingFailedException(EmailSendingFailedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Object> handleEmailNotVerifiedException(EmailNotVerifiedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Object> handleAuthenticationFailedException(AuthenticationFailedException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

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
