package com.lab.backend.auth.dto.responses;

/**
 * A response object representing the authentication result.
 *
 * @param accessToken  the access token granted after successful authentication.
 * @param refreshToken the refresh token granted after successful authentication.
 * @param authStatus   the status of the authentication process.
 * @author Ömer Asaf BALIKÇI
 */

public record AuthResponse(String accessToken, String refreshToken, AuthStatus authStatus) {
}
