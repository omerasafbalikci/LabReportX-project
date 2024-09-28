package com.lab.backend.auth.service.abstracts;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface JwtService {
    String generateAccessToken(String username, List<String> roles);

    String generateRefreshToken(String username);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
