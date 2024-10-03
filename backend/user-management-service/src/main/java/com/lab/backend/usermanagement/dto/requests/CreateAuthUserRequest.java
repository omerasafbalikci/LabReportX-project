package com.lab.backend.usermanagement.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO for auth user used as input.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
public class CreateAuthUserRequest {
    private String username;
    private String password;
    private String email;
    private List<String> roles;
}
