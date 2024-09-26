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

@Component
@Log4j2
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {
    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthGatewayFilterFactory(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (this.routeValidator.isSecured.test(request)) {
                String token = exchange.getRequest().getHeaders().getFirst("Authorization");

                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    Claims claims = this.jwtUtil.getClaimsAndValidate(token);
                    if (claims == null) {
                        throw new InvalidTokenException("Invalid token");
                    }

                    if (this.jwtUtil.isLoggedOut(token)) {
                        throw new LoggedOutTokenException("Token is logged out");
                    }

                    String username = claims.getSubject();

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-Username", username)
                            .build();
                    exchange = exchange.mutate().request(modifiedRequest).build();

                    if (this.routeValidator.isRoleBasedAuthorizationNeeded.test(request)) {
                        List<String> roles = this.jwtUtil.getRoles(claims);

                        if (roles == null || roles.isEmpty()) {
                            throw new MissingRolesException("No roles found in token");
                        }

                        String fullPath = request.getPath().toString();
                        List<String> requiredRoles = config.getRoleMapping().get(fullPath);
                        if (requiredRoles == null) {
                            String basePath = fullPath.split("/")[1];
                            requiredRoles = config.getRoleMapping().get("/" + basePath);
                        }

                        if (roles.stream().noneMatch(requiredRoles::contains)) {
                            throw new InsufficientRolesException("Insufficient roles");
                        }
                    }
                    return chain.filter(exchange);
                } else {
                    throw new MissingAuthorizationHeaderException("Missing or invalid authorization header");
                }
            } else {
                return chain.filter(exchange);
            }
        };
    }

    @Getter
    public static class Config {
        private Map<String, List<String>> roleMapping;

        public Config setRoleMapping(Map<String, List<String>> roleMapping) {
            this.roleMapping = roleMapping;
            return this;
        }
    }
}
