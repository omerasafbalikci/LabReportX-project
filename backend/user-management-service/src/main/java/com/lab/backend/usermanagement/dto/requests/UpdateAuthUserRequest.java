package com.lab.backend.usermanagement.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateAuthUserRequest {
    @NotNull(message = "Id must not be null")
    private Long id;
    private String username;
}