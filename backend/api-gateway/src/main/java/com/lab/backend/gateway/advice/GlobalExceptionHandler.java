package com.lab.backend.gateway.advice;

import com.lab.backend.gateway.utilities.exceptions.*;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {
    public GlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageWriters(ServerCodecConfigurer.create().getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        HttpStatus status = determineHttpStatus(error);
        ErrorResponse errorResponse = new ErrorResponse(status, error.getMessage(), request.path());

        return ServerResponse.status(status).bodyValue(errorResponse);
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        if (error instanceof TokenNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (error instanceof InsufficientRolesException) {
            return HttpStatus.FORBIDDEN;
        } else if (error instanceof MissingAuthorizationHeaderException) {
            return HttpStatus.BAD_REQUEST;
        } else if (error instanceof InvalidTokenException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (error instanceof MissingRolesException || error instanceof LoggedOutTokenException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (error instanceof AuthServiceUnavailableException || error instanceof UserServiceUnavailableException ||
                error instanceof PatientServiceUnavailableException || error instanceof ReportServiceUnavailableException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
