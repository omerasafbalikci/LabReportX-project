package com.lab.backend.usermanagement.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAuthUserRequest {
    private Long id;
    private String username;
}
