package com.lab.backend.auth.service.abstracts;

import com.lab.backend.auth.dto.requests.AuthRequest;
import com.lab.backend.auth.dto.requests.PasswordRequest;
import com.lab.backend.auth.utilities.exceptions.InvalidTokenException;
import com.lab.backend.auth.utilities.exceptions.RedisOperationException;
import com.lab.backend.auth.utilities.exceptions.UsernameExtractionException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service interface for handling user authentication, password management, and token management.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface UserService {
    /**
     * Logs a user in using the provided authentication request and returns access and refresh tokens.
     *
     * @param authRequest the authentication request containing the username and password
     * @return a list containing the access token and refresh token
     * @throws RedisOperationException if an error occurs during Redis operations
     */
    List<String> login(AuthRequest authRequest) throws RedisOperationException;

    /**
     * Refreshes the user's access token using the refresh token from the request.
     *
     * @param request the HTTP request containing the refresh token
     * @return a list containing the new access token and refresh token
     * @throws UsernameExtractionException if the username cannot be extracted from the token
     * @throws InvalidTokenException       if the provided token is invalid
     * @throws RedisOperationException     if an error occurs during Redis operations
     */
    List<String> refreshToken(HttpServletRequest request) throws UsernameExtractionException, InvalidTokenException, RedisOperationException;

    /**
     * Logs a user out by invalidating the relevant tokens.
     *
     * @param request the HTTP request containing the access token
     * @throws InvalidTokenException   if the provided token is invalid
     * @throws RedisOperationException if an error occurs during Redis operations
     */
    void logout(HttpServletRequest request) throws InvalidTokenException, RedisOperationException;

    /**
     * Changes the password of a user, requiring the current password and the new password.
     *
     * @param request         the HTTP request containing the access token
     * @param passwordRequest the password change request containing the current and new passwords
     * @return a string message indicating the result of the password change
     * @throws RedisOperationException if an error occurs during Redis operations
     * @throws InvalidTokenException   if the provided token is invalid
     */
    String changePassword(HttpServletRequest request, PasswordRequest passwordRequest) throws RedisOperationException, InvalidTokenException;

    /**
     * Initiates the password reset process by sending a reset link to the user's email.
     *
     * @param email the email address of the user requesting the password reset
     */
    void initiatePasswordReset(String email);

    /**
     * Handles the password reset process using the reset token and new password.
     *
     * @param token       the password reset token
     * @param newPassword the new password to be set
     * @return a string message indicating the result of the password reset
     */
    String handlePasswordReset(String token, String newPassword);

    /**
     * Verifies the user's email using an email verification token.
     *
     * @param token the email verification token
     */
    void verifyEmail(String token);
}
