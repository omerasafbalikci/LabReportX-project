package com.lab.backend.gateway.config;

import com.lab.backend.gateway.utilities.JwtUtil;
import com.lab.backend.gateway.utilities.exceptions.*;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthGatewayFilterFactoryTest {
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RouteValidator routeValidator;
    @Mock
    private ServerWebExchange exchange;
    @Mock
    private ServerHttpRequest request;
    @Mock
    private ServerHttpResponse response;
    @Mock
    private GatewayFilterChain chain;
    @Mock
    private Predicate<ServerHttpRequest> isSecured;
    @Mock
    private Predicate<ServerHttpRequest> isRoleBasedAuthorizationNeeded;
    @InjectMocks
    private AuthGatewayFilterFactory filterFactory;

    private AuthGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        this.config = new AuthGatewayFilterFactory.Config();
        config.setRoleMapping(Collections.singletonMap("/user/me", Arrays.asList("ROLE_ADMIN", "ROLE_SECRETARY")));

        when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);

        this.routeValidator.isSecured = isSecured;
        this.routeValidator.isRoleBasedAuthorizationNeeded = isRoleBasedAuthorizationNeeded;
    }

    @Test
    void givenOpenEndpoint_whenApply_thenChainFilterIsCalled() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(false);

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);
        filter.filter(exchange, chain);

        // Assert
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void givenMissingAuthorizationHeader_whenApply_thenThrowsMissingAuthorizationHeaderException() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);

        // Assert
        assertThrows(MissingAuthorizationHeaderException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    void givenInvalidAuthorizationHeader_whenApply_thenThrowsMissingAuthorizationHeaderException() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "InvalidToken");
        when(request.getHeaders()).thenReturn(headers);

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);

        // Assert
        assertThrows(MissingAuthorizationHeaderException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    void givenInvalidToken_whenApply_thenThrowsInvalidTokenException() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer InvalidToken");
        when(request.getHeaders()).thenReturn(headers);
        when(jwtUtil.getClaimsAndValidate("InvalidToken")).thenReturn(null);

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);

        // Assert
        assertThrows(InvalidTokenException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    void givenLoggedOutToken_whenApply_thenThrowsLoggedOutTokenException() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer LoggedOutToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtil.getClaimsAndValidate("LoggedOutToken")).thenReturn(claims);
        when(jwtUtil.isLoggedOut("LoggedOutToken")).thenReturn(true);

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);

        // Assert
        assertThrows(LoggedOutTokenException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    void givenMissingRoles_whenApply_thenThrowsMissingRolesException() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ValidToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtil.getClaimsAndValidate("ValidToken")).thenReturn(claims);
        when(jwtUtil.isLoggedOut("ValidToken")).thenReturn(false);
        String mockUsername = "testuser";
        when(claims.getSubject()).thenReturn(mockUsername);
        when(isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtil.getRoles(claims)).thenReturn(Collections.emptyList());
        ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header("X-Username", mockUsername)).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(request)).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);

        // Assert
        assertThrows(MissingRolesException.class, () -> filter.filter(exchange, chain));

        // Verify
        verify(requestBuilder, times(1)).header("X-Username", mockUsername);
    }

    @Test
    void givenInsufficientRoles_whenApply_thenThrowsInsufficientRolesException() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ValidToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtil.getClaimsAndValidate("ValidToken")).thenReturn(claims);
        when(jwtUtil.isLoggedOut("ValidToken")).thenReturn(false);
        String mockUsername = "testuser";
        when(claims.getSubject()).thenReturn(mockUsername);
        when(isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
        ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header("X-Username", mockUsername)).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(request)).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
        when(jwtUtil.getRoles(claims)).thenReturn(Collections.singletonList("ROLE_TECHNICIAN"));
        when(request.getPath()).thenReturn(mock(RequestPath.class));
        when(request.getPath().toString()).thenReturn("/user/me");

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);

        // Assert
        assertThrows(InsufficientRolesException.class, () -> filter.filter(exchange, chain));

        // Verify
        verify(requestBuilder, times(1)).header("X-Username", mockUsername);
    }

    @Test
    void givenValidTokenAndRoles_whenApply_thenChainFilterIsCalled() {
        // Arrange
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ValidToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtil.getClaimsAndValidate("ValidToken")).thenReturn(claims);
        when(jwtUtil.isLoggedOut("ValidToken")).thenReturn(false);
        String mockUsername = "testuser";
        when(claims.getSubject()).thenReturn(mockUsername);
        when(isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(false);
        ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header("X-Username", mockUsername)).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(request)).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        // Act
        GatewayFilter filter = this.filterFactory.apply(config);
        filter.filter(exchange, chain);

        // Verify
        verify(requestBuilder, times(1)).header("X-Username", mockUsername);
        verify(chain, times(1)).filter(exchange);
    }
}
