package com.lab.backend.auth.dto.responses;

public record AuthResponse(String accessToken, String refreshToken, AuthStatus authStatus) {
}
