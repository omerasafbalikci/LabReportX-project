package com.lab.backend.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GatewayConfig is a configuration class for setting up routes and route filters in the Spring Cloud Gateway.
 * It also defines role-based access control using the {@link AuthGatewayFilterFactory}.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Configuration
public class GatewayConfig {
    @Value("${route.auth}")
    private String AUTH_URI;

    @Value("${route.user}")
    private String USER_URI;

    @Value("${route.patient}")
    private String PATIENT_URI;

    @Value("${route.report}")
    private String REPORT_URI;

    @Value("${route.analytics}")
    private String ANALYTICS_URI;

    @Value("${circuit-breaker-name}")
    private String CIRCUIT_BREAKER_NAME;

    private static final String ADMIN = "ADMIN";
    private static final String TECHNICIAN = "TECHNICIAN";
    private static final String SECRETARY = "SECRETARY";
    private final Map<String, List<String>> endpointRoleMapping = new HashMap<>();

    /**
     * Constructs a GatewayConfig object and sets up the role mappings for various endpoints.
     * Role mappings define which user roles are allowed to access specific endpoints.
     */
    public GatewayConfig() {
        this.endpointRoleMapping.put("/patients", List.of(SECRETARY));
        this.endpointRoleMapping.put("/patients/tr-id-number", List.of(SECRETARY, TECHNICIAN));
        this.endpointRoleMapping.put("/patients/email", List.of(SECRETARY, TECHNICIAN));
        this.endpointRoleMapping.put("/patients/check-tr-id-number", List.of(SECRETARY, TECHNICIAN));
        this.endpointRoleMapping.put("/barcode", List.of(SECRETARY));
        this.endpointRoleMapping.put("/reports", List.of(TECHNICIAN));
        this.endpointRoleMapping.put("/analytics", List.of(ADMIN));
        this.endpointRoleMapping.put("/users", List.of(ADMIN));
        this.endpointRoleMapping.put("/users/me", List.of(SECRETARY, TECHNICIAN, ADMIN));
        this.endpointRoleMapping.put("/users/update/me", List.of(SECRETARY, TECHNICIAN, ADMIN));
        this.endpointRoleMapping.put("/auth/refresh", List.of(SECRETARY, TECHNICIAN, ADMIN));
        this.endpointRoleMapping.put("/auth/logout", List.of(SECRETARY, TECHNICIAN, ADMIN));
        this.endpointRoleMapping.put("/auth/change-password", List.of(SECRETARY, TECHNICIAN, ADMIN));
    }

    /**
     * Defines the routes for the application, setting up path matching, role-based authorization filters,
     * and circuit breakers with fallback URIs for different services (auth, user management, patient, report).
     *
     * @param builder                  the RouteLocatorBuilder for defining routes and their filters.
     * @param authGatewayFilterFactory the custom filter factory for JWT validation and role-based authorization.
     * @return the constructed RouteLocator containing all routes and their configurations.
     */
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthGatewayFilterFactory authGatewayFilterFactory) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri(this.AUTH_URI))

                .route("user-management-service", r -> r.path("/users/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/user"))
                        )
                        .uri(this.USER_URI))

                .route("patient-service", r -> r.path("/patients/**", "/barcode/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/patient"))
                        )
                        .uri(this.PATIENT_URI))

                .route("report-service", r -> r.path("/reports/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/report"))
                        )
                        .uri(this.REPORT_URI))

                .route("analytics-service", r -> r.path("/analytics/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/analytics"))
                        )
                        .uri(this.ANALYTICS_URI))

                .build();
    }
}
