package com.lab.backend.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * RouteValidator is a utility class responsible for determining which routes require security checks
 * and role-based authorization in the application. It uses predefined open endpoints to bypass security checks
 * and allows flexible configurations for secured routes.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
public class RouteValidator {
    public static final List<String> openEndpoints = List.of(
            "/auth"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    public Predicate<ServerHttpRequest> isRoleBasedAuthorizationNeeded =
            request -> true;
}
