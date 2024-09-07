package com.lab.backend.usermanagement.dto.requests;

import com.lab.backend.usermanagement.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @NotNull(message = "Id must not be null")
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    @Email(message = "It must be a valid email")
    private String email;
    private Gender gender;
}
