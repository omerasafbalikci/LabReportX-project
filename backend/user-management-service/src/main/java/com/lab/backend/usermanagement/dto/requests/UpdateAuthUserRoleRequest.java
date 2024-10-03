package com.lab.backend.usermanagement.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO to update auth user role.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
public class UpdateAuthUserRoleRequest {
    private String username;
    private String role;
}
