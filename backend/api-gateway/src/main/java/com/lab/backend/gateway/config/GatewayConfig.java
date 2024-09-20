package com.lab.backend.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @Value("${circuit-breaker-name}")
    private String CIRCUIT_BREAKER_NAME;

    private final Map<String, Set<String>> endpointRoleMapping = new HashMap<>();

    public GatewayConfig() {
        this.endpointRoleMapping.put("/patients", Set.of("SECRETARY"));
        this.endpointRoleMapping.put("/barcode", Set.of("SECRETARY"));
        this.endpointRoleMapping.put("/reports", Set.of("TECHNICIAN"));
        this.endpointRoleMapping.put("/users", Set.of("ADMIN"));
        this.endpointRoleMapping.put("/users/me", Set.of("SECRETARY", "TECHNICIAN", "ADMIN"));
        this.endpointRoleMapping.put("/users/update/me", Set.of("SECRETARY", "TECHNICIAN", "ADMIN"));
        this.endpointRoleMapping.put("/auth/change-password", Set.of("SECRETARY", "TECHNICIAN", "ADMIN"));
        this.endpointRoleMapping.put("/auth/refresh", Set.of("SECRETARY", "TECHNICIAN", "ADMIN"));
        this.endpointRoleMapping.put("/auth/logout", Set.of("SECRETARY", "TECHNICIAN", "ADMIN"));
    }

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

                .route("patient-service", r -> r.path("/patients/**")
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

                .build();
    }
}
