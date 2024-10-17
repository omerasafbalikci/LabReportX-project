package com.lab.backend.auth.service;

import com.lab.backend.auth.service.concretes.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {
    @InjectMocks
    private JwtServiceImpl jwtService;
    @Mock
    private UserDetails userDetails;
    private final long accessTokenExpiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        final String secretKey = "testsignerkeytestsignerkeytestsignerkeytestsignerkey";
        final String authoritiesKey = "roles";
        final long refreshTokenExpiration = 1000 * 60 * 60 * 24; // 24 hours

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", secretKey);
        ReflectionTestUtils.setField(jwtService, "AUTHORITIES_KEY", authoritiesKey);
        ReflectionTestUtils.setField(jwtService, "ACCESS_TOKEN_EXPIRATION", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtService, "REFRESH_TOKEN_EXPIRATION", refreshTokenExpiration);
    }

    @Test
    void givenValidToken_whenExtractUsername_thenUsernameIsExtracted() {
        // Act
        String token = this.jwtService.generateAccessToken("user", List.of("ROLE_ADMIN"));
        String username = this.jwtService.extractUsername(token);

        // Assert
        assertEquals("user", username);
    }

    @Test
    void givenValidTokenAndUser_whenIsValid_thenReturnsTrue() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("user");

        // Act
        String token = this.jwtService.generateAccessToken("user", List.of("ROLE_ADMIN"));

        // Assert
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void givenValidToken_whenExtractClaim_thenCorrectClaimIsExtracted() {
        // Act
        String token = jwtService.generateAccessToken("user", List.of("ROLE_USER"));
        String username = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("user", username);
    }

    @Test
    void givenValidUserAndRoles_whenGenerateAccessToken_thenTokenIsGenerated() {
        // Act
        String token = jwtService.generateAccessToken("user", List.of("ROLE_USER"));

        // Assert
        assertNotNull(token);
    }

    @Test
    void givenValidUser_whenGenerateRefreshToken_thenTokenIsGenerated() {
        // Act
        String token = jwtService.generateRefreshToken("user");

        // Assert
        assertNotNull(token);
    }

    @Test
    void givenValidClaimsAndExpiration_whenBuildToken_thenTokenIsBuilt() {
        // Arrange
        Claims claims = Jwts.claims().subject("user").build();

        // Act
        String token = jwtService.buildToken(claims, accessTokenExpiration);

        // Assert
        assertNotNull(token);
    }

    @Test
    void givenInvalidToken_whenExtractUsername_thenThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(invalidToken));
    }
}
