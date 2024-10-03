package com.lab.backend.auth.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO to update user role.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
public class UpdateAuthUserRoleRequest {
    private String username;
    private String role;
}
