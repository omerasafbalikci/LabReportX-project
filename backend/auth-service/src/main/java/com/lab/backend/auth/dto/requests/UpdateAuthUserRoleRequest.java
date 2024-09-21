package com.lab.backend.auth.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAuthUserRoleRequest {
    private Long id;
    private String role;
}
