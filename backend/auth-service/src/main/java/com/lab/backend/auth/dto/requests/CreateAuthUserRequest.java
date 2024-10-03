package com.lab.backend.auth.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO for user used as input.
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
