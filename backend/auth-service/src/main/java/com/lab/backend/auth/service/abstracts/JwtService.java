package com.lab.backend.auth.service.abstracts;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(UserDetails userDetails);

    String extractUsername(String jwt);

    String extractTokenId(String jwt);

    boolean isTokenValid(String jwt, UserDetails userDetails);
}
