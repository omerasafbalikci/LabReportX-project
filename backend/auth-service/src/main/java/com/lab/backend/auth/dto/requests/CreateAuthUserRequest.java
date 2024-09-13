package com.lab.backend.auth.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class CreateAuthUserRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must have at least 8 characters")
    private String password;
    @Email(message = "It must be a valid email")
    @NotBlank(message = "Email must not be blank")
    private String email;
    @Size(min = 1, message = "User must have at least one role")
    @NotNull(message = "Role must not be null")
    private Set<String> roles;
}
