package com.lab.backend.usermanagement.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAuthUserRoleRequest {
    @NotNull(message = "Id must not be null")
    private Long id;
    @NotBlank(message = "Role must not be blank")
    private String role;
}
