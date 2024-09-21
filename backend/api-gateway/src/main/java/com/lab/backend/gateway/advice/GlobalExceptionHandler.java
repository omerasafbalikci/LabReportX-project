package com.lab.backend.gateway.advice;

import com.lab.backend.gateway.utilities.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientRolesException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientRolesException(InsufficientRolesException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingAuthorizationHeaderException(MissingAuthorizationHeaderException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MissingRolesException.class)
    public ResponseEntity<ErrorResponse> handleMissingRolesException(MissingRolesException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LoggedOutTokenException.class)
    public ResponseEntity<ErrorResponse> handleLoggedOutTokenException(LoggedOutTokenException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAuthServiceUnavailableException(AuthServiceUnavailableException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceUnavailableException(UserServiceUnavailableException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(PatientServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handlePatientServiceUnavailableException(PatientServiceUnavailableException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ReportServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleReportServiceUnavailableException(ReportServiceUnavailableException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
