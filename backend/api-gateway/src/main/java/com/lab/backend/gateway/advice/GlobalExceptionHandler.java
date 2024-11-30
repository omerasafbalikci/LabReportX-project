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

/**
 * GlobalExceptionHandler handles exceptions in a Spring WebFlux application and
 * returns custom error responses based on the type of exception.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {
    /**
     * Constructor to initialize GlobalExceptionHandler with necessary components.
     *
     * @param errorAttributes    the error attributes containing error details.
     * @param webProperties      properties to configure web resources.
     * @param applicationContext the application context.
     */
    public GlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageWriters(ServerCodecConfigurer.create().getWriters());
    }

    /**
     * Defines a routing function that matches all requests and handles them with
     * the {@link #renderErrorResponse(ServerRequest)} method.
     *
     * @param errorAttributes the error attributes to use for error handling.
     * @return the RouterFunction that routes the error requests.
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * Renders the error response based on the type of error that occurred.
     *
     * @param request the server request that triggered the error.
     * @return a Mono of ServerResponse with a structured error response.
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        HttpStatus status = determineHttpStatus(error);
        ErrorResponse errorResponse = new ErrorResponse(status, error.getMessage(), request.path());

        return ServerResponse.status(status).bodyValue(errorResponse);
    }

    /**
     * Determines the appropriate HTTP status code based on the type of exception.
     *
     * @param error the exception that was thrown.
     * @return the corresponding HTTP status.
     */
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
                error instanceof PatientServiceUnavailableException || error instanceof ReportServiceUnavailableException ||
                error instanceof AnalyticsServiceUnavailableException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
