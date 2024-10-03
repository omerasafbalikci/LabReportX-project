package com.lab.backend.auth.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO to update user.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
public class UpdateAuthUserRequest {
    private String oldUsername;
    private String newUsername;
}
