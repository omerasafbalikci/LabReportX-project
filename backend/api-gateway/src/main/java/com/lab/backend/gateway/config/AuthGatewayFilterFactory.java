package com.lab.backend.gateway.config;

import com.lab.backend.gateway.utilities.JwtUtil;
import com.lab.backend.gateway.utilities.exceptions.*;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AuthGatewayFilterFactory is a custom Gateway filter that performs token validation and role-based authorization.
 * It intercepts requests to secure routes and checks for the presence of a valid JWT token.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@Log4j2
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {
    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    /**
     * Constructs the filter with the necessary dependencies.
     *
     * @param jwtUtil        utility class for handling JWT operations.
     * @param routeValidator utility for determining secured routes and role-based access.
     */
    public AuthGatewayFilterFactory(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    /**
     * Applies the filtering logic for handling authentication and authorization for secured routes.
     *
     * @param config configuration object containing role mappings.
     * @return a GatewayFilter that handles JWT validation and role checks.
     */
    @Override
    public GatewayFilter apply(Config config) {
        log.trace("Entering apply method in AuthGatewayFilterFactory class");
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("Processing request for path: {}", request.getPath());

            if (this.routeValidator.isSecured.test(request)) {
                log.debug("Request is to a secured route: {}", request.getPath());
                String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    log.debug("Extracted token: {}", token);
                    Claims claims = this.jwtUtil.getClaimsAndValidate(token);
                    if (claims == null) {
                        log.error("Invalid token for request path: {}", request.getPath());
                        throw new InvalidTokenException("Invalid token");
                    }
                    if (this.jwtUtil.isLoggedOut(token)) {
                        log.error("Token is logged out: {}", token);
                        throw new LoggedOutTokenException("Token is logged out");
                    }

                    String username = claims.getSubject();
                    log.info("Authenticated user: {}", username);
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-Username", username)
                            .build();
                    exchange = exchange.mutate().request(modifiedRequest).build();

                    if (this.routeValidator.isRoleBasedAuthorizationNeeded.test(request)) {
                        log.debug("Role-based authorization needed for path: {}", request.getPath());
                        List<String> roles = this.jwtUtil.getRoles(claims);
                        if (roles == null || roles.isEmpty()) {
                            log.error("No roles found in token for user: {}", username);
                            throw new MissingRolesException("No roles found in token");
                        }
                        String fullPath = request.getPath().toString();
                        List<String> requiredRoles = config.getRoleMapping().get(fullPath);
                        if (requiredRoles == null) {
                            String basePath = fullPath.split("/")[1];
                            requiredRoles = config.getRoleMapping().get("/" + basePath);
                        }
                        if (roles.stream().noneMatch(requiredRoles::contains)) {
                            log.error("User {} does not have sufficient roles for path: {}", username, fullPath);
                            throw new InsufficientRolesException("Insufficient roles");
                        }
                        log.info("User {} authorized for path: {}", username, fullPath);
                    }
                    log.trace("Exiting apply method in AuthGatewayFilterFactory class with successful authentication");
                    return chain.filter(exchange);
                } else {
                    log.error("Missing or invalid authorization header for request path: {}", request.getPath());
                    throw new MissingAuthorizationHeaderException("Missing or invalid authorization header");
                }
            } else {
                log.debug("Request is to an unsecured route: {}", request.getPath());
                return chain.filter(exchange);
            }
        };
    }

    /**
     * Config class that holds the role mapping configuration used by the AuthGatewayFilterFactory.
     */
    @Getter
    public static class Config {
        private Map<String, List<String>> roleMapping;

        /**
         * Sets the role mapping configuration.
         *
         * @param roleMapping a map of route paths to the required roles.
         * @return the current Config instance.
         */
        public Config setRoleMapping(Map<String, List<String>> roleMapping) {
            this.roleMapping = roleMapping;
            return this;
        }
    }
}
