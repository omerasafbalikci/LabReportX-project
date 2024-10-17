package com.lab.backend.auth.controller;

import com.lab.backend.auth.dto.requests.AuthRequest;
import com.lab.backend.auth.dto.requests.PasswordRequest;
import com.lab.backend.auth.dto.responses.AuthResponse;
import com.lab.backend.auth.dto.responses.AuthStatus;
import com.lab.backend.auth.service.abstracts.UserService;
import com.lab.backend.auth.utilities.exceptions.InvalidTokenException;
import com.lab.backend.auth.utilities.exceptions.RedisOperationException;
import com.lab.backend.auth.utilities.exceptions.UsernameExtractionException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;
    private AuthRequest authRequest;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        this.authRequest = new AuthRequest();
        this.authRequest.setUsername("testUser");
        this.authRequest.setPassword("testPassword");
        this.httpServletRequest = mock(HttpServletRequest.class);
    }

    @Test
    void testLogin_success() throws RedisOperationException {
        // Arrange
        List<String> tokens = List.of("accessToken", "refreshToken");
        when(userService.login(authRequest)).thenReturn(tokens);

        // Act
        ResponseEntity<AuthResponse> response = this.userController.login(authRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AuthStatus.LOGIN_SUCCESS, response.getBody().authStatus());
        assertEquals("accessToken", response.getBody().accessToken());
        assertEquals("refreshToken", response.getBody().refreshToken());
    }

    @Test
    void testRefreshToken_success() throws RedisOperationException, InvalidTokenException, UsernameExtractionException {
        // Arrange
        List<String> tokens = List.of("newAccessToken", "newRefreshToken");
        when(userService.refreshToken(httpServletRequest)).thenReturn(tokens);

        // Act
        ResponseEntity<AuthResponse> response = this.userController.refreshToken(httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AuthStatus.TOKEN_REFRESHED_SUCCESSFULLY, response.getBody().authStatus());
        assertEquals("newAccessToken", response.getBody().accessToken());
        assertEquals("newRefreshToken", response.getBody().refreshToken());
    }

    @Test
    void testLogout_success() throws InvalidTokenException, RedisOperationException {
        // Arrange
        doNothing().when(userService).logout(httpServletRequest);

        // Act
        ResponseEntity<String> response = this.userController.logout(httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out successfully.", response.getBody());
    }

    @Test
    void testChangePassword_success() throws RedisOperationException, InvalidTokenException {
        // Arrange
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("newPassword123");
        when(userService.changePassword(httpServletRequest, passwordRequest)).thenReturn("Password changed successfully");

        // Act
        ResponseEntity<String> response = this.userController.changePassword(httpServletRequest, passwordRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully", response.getBody());
    }

    @Test
    void testInitiatePasswordReset_success() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(userService).initiatePasswordReset(email);

        // Act
        ResponseEntity<String> response = this.userController.initiatePasswordReset(email);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset initiated. Check your email for further instructions.", response.getBody());
    }

    @Test
    void testHandlePasswordReset_success() {
        // Arrange
        String token = "resetToken";
        String newPassword = "newPassword123";
        when(userService.handlePasswordReset(token, newPassword)).thenReturn("Password reset successful");

        // Act
        ResponseEntity<String> response = this.userController.handlePasswordReset(token, newPassword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successful", response.getBody());
    }

    @Test
    void testVerifyEmail_success() {
        // Arrange
        String token = "verificationToken";
        doNothing().when(userService).verifyEmail(token);

        // Act
        ResponseEntity<String> response = this.userController.verifyEmail(token);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email verified successfully.", response.getBody());
    }
}
