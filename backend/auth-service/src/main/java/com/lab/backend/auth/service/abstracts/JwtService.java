package com.lab.backend.auth.service.abstracts;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Service interface for handling JWT (JSON Web Token) operations such as generation, validation, and extraction.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface JwtService {
    /**
     * Generates an access token for a given username and roles.
     *
     * @param username the username for whom the token is generated
     * @param roles    the list of roles assigned to the user
     * @return a newly generated access token
     */
    String generateAccessToken(String username, List<String> roles);

    /**
     * Generates a refresh token for a given username.
     *
     * @param username the username for whom the refresh token is generated
     * @return a newly generated refresh token
     */
    String generateRefreshToken(String username);

    /**
     * Extracts the username from a given JWT token.
     *
     * @param token the JWT token
     * @return the extracted username
     */
    String extractUsername(String token);

    /**
     * Validates whether the given token is valid for the provided user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate the token against
     * @return true if the token is valid, otherwise false
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}
