package com.lab.backend.auth.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAuthUserRequest {
    private String oldUsername;
    private String newUsername;
}
