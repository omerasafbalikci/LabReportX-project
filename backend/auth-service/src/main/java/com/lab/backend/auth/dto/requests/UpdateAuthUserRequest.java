package com.lab.backend.auth.dto.requests;

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