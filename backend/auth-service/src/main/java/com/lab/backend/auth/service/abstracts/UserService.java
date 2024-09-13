package com.lab.backend.auth.service.abstracts;

import com.lab.backend.auth.dto.requests.AuthRequest;
import com.lab.backend.auth.dto.requests.PasswordRequest;
import com.lab.backend.auth.utilities.exceptions.RedisOperationException;
import com.lab.backend.auth.utilities.exceptions.UnauthorizedException;
import com.lab.backend.auth.utilities.exceptions.UsernameExtractionException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    List<String> login(AuthRequest authRequest) throws RedisOperationException;

    List<String> refreshToken(HttpServletRequest request) throws UsernameExtractionException, UnauthorizedException, RedisOperationException;

    void logout(HttpServletRequest request) throws UnauthorizedException, RedisOperationException;

    String changePassword(HttpServletRequest request, PasswordRequest passwordRequest) throws RedisOperationException, UnauthorizedException;

    void initiatePasswordReset(String email);

    String handlePasswordReset(String token, String newPassword);

    void verifyEmail(String token);
}
