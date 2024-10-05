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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;

    /**
     * Handles user login requests.
     *
     * @param authRequest the authentication request containing username and password.
     * @return a response entity containing authentication tokens and status.
     * @throws RedisOperationException if there's an error with Redis operations.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) throws RedisOperationException {
        log.trace("Entering login method in UserController");
        var tokens = this.userService.login(authRequest);
        var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.LOGIN_SUCCESS);
        log.trace("Exiting login method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Refreshes the authentication token.
     *
     * @param request the HTTP request containing the old token.
     * @return a response entity containing new authentication tokens and status.
     * @throws RedisOperationException     if there's an error with Redis operations.
     * @throws InvalidTokenException       if the provided token is invalid.
     * @throws UsernameExtractionException if there's an error extracting the username.
     */
    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) throws RedisOperationException, InvalidTokenException, UsernameExtractionException {
        log.trace("Entering refreshToken method in UserController");
        var tokens = this.userService.refreshToken(request);
        var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.TOKEN_REFRESHED_SUCCESSFULLY);
        log.trace("Exiting refreshToken method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    /**
     * Handles user logout requests.
     *
     * @param request the HTTP request containing the token.
     * @return a response entity indicating logout success.
     * @throws InvalidTokenException   if the provided token is invalid.
     * @throws RedisOperationException if there's an error with Redis operations.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) throws InvalidTokenException, RedisOperationException {
        log.trace("Entering logout method in UserController");
        this.userService.logout(request);
        log.trace("Exiting logout method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully.");
    }

    /**
     * Changes the user's password.
     *
     * @param request         the HTTP request containing the token.
     * @param passwordRequest the request containing the new password.
     * @return a response entity indicating the result of the password change.
     * @throws RedisOperationException if there's an error with Redis operations.
     * @throws InvalidTokenException   if the provided token is invalid.
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(HttpServletRequest request, @RequestBody @Valid PasswordRequest passwordRequest) throws RedisOperationException, InvalidTokenException {
        log.trace("Entering changePassword method in UserController");
        String response = this.userService.changePassword(request, passwordRequest);
        log.trace("Exiting changePassword method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Initiates the password reset process by sending an email.
     *
     * @param email the email address of the user.
     * @return a response entity indicating the result of the password reset initiation.
     */
    @PostMapping("/initiate-password-reset")
    public ResponseEntity<String> initiatePasswordReset(@RequestParam("email") String email) {
        log.trace("Entering initiatePasswordReset method in UserController");
        this.userService.initiatePasswordReset(email);
        log.trace("Exiting initiatePasswordReset method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body("Password reset initiated. Check your email for further instructions.");
    }

    /**
     * Handles the password reset process.
     *
     * @param token       the reset token.
     * @param newPassword the new password.
     * @return a response entity indicating the result of the password reset.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> handlePasswordReset(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        log.trace("Entering handlePasswordReset method in UserController");
        String responseMessage = this.userService.handlePasswordReset(token, newPassword);
        log.trace("Exiting handlePasswordReset method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    /**
     * Verifies the user's email address using a verification token.
     *
     * @param token the verification token.
     * @return a response entity indicating the result of the email verification.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        log.trace("Entering verifyEmail method in UserController");
        this.userService.verifyEmail(token);
        log.trace("Exiting verifyEmail method in UserController");
        return ResponseEntity.status(HttpStatus.OK).body("Email verified successfully.");
    }
}
