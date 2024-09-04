package com.lab.backend.usermanagement.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserRequest {
    private String username;
    private String password;
    private Set<String> roles;
}
