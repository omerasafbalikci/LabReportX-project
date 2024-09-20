package com.lab.backend.usermanagement.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class CreateAuthUserRequest {
    private String username;
    private String password;
    private String email;
    private Set<String> roles;
}
