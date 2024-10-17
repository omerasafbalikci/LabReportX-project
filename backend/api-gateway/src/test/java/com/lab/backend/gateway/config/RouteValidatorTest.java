package com.lab.backend.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouteValidatorTest {
    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        this.routeValidator = new RouteValidator();
    }

    @Test
    void testIsOpenEndpointWithOpenEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();

        // Assert
        assertFalse(routeValidator.isSecured.test(request));
    }

    @Test
    void testIsOpenEndpointWithNonOpenEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/some-other-endpoint").build();

        // Assert
        assertTrue(routeValidator.isSecured.test(request));
    }

    @Test
    void testIsRoleBasedAuthorizationNeededWithNonNoRoleBasedAuthorizationEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/some-other-endpoint").build();

        // Assert
        assertTrue(routeValidator.isRoleBasedAuthorizationNeeded.test(request));
    }
}
