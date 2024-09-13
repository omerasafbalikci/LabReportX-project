package com.lab.backend.auth.controller;

import com.lab.backend.auth.dto.requests.AuthRequest;
import com.lab.backend.auth.dto.requests.PasswordRequest;
import com.lab.backend.auth.dto.responses.AuthResponse;
import com.lab.backend.auth.dto.responses.AuthStatus;
import com.lab.backend.auth.service.abstracts.UserService;
import com.lab.backend.auth.utilities.exceptions.RedisOperationException;
import com.lab.backend.auth.utilities.exceptions.UnauthorizedException;
import com.lab.backend.auth.utilities.exceptions.UsernameExtractionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) throws RedisOperationException {
        var tokens = this.userService.login(authRequest);
        var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.LOGIN_SUCCESS);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) throws RedisOperationException, UnauthorizedException, UsernameExtractionException {
        var tokens = this.userService.refreshToken(request);
        var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.TOKEN_REFRESHED_SUCCESSFULLY);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) throws UnauthorizedException, RedisOperationException {
        this.userService.logout(request);
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(HttpServletRequest request, @RequestBody PasswordRequest passwordRequest) throws RedisOperationException, UnauthorizedException {
        String response = this.userService.changePassword(request, passwordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/initiate-password-reset")
    public ResponseEntity<String> initiatePasswordReset(@RequestParam("email") String email) {
        this.userService.initiatePasswordReset(email);
        return ResponseEntity.status(HttpStatus.OK).body("Password reset initiated. Check your email for further instructions.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> handlePasswordReset(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        String responseMessage = this.userService.handlePasswordReset(token, newPassword);
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        this.userService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body("Email verified successfully.");
    }
}
