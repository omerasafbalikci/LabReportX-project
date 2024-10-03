package com.lab.backend.usermanagement.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO to update auth user.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
public class UpdateAuthUserRequest {
    private String oldUsername;
    private String newUsername;
}
