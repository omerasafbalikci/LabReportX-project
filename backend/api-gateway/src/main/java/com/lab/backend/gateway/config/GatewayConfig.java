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
    private String authUri;

    @Value("${route.user}")
    private String userUri;

    @Value("${route.patient}")
    private String patientUri;

    @Value("${route.report}")
    private String reportUri;

    @Value("${route.analytics}")
    private String analyticsUri;

    @Value("${circuit-breaker-name}")
    private String circuitBreakerName;

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
                                .circuitBreaker(c -> c.setName(this.circuitBreakerName).setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri(this.authUri))

                .route("user-management-service", r -> r.path("/users/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.circuitBreakerName).setFallbackUri("forward:/fallback/user"))
                        )
                        .uri(this.userUri))

                .route("patient-service", r -> r.path("/patients/**", "/barcode/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.circuitBreakerName).setFallbackUri("forward:/fallback/patient"))
                        )
                        .uri(this.patientUri))

                .route("report-service", r -> r.path("/reports/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.circuitBreakerName).setFallbackUri("forward:/fallback/report"))
                        )
                        .uri(this.reportUri))

                .route("analytics-service", r -> r.path("/analytics/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(this.circuitBreakerName).setFallbackUri("forward:/fallback/analytics"))
                        )
                        .uri(this.analyticsUri))

                .build();
    }
}
