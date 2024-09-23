package com.lab.backend.usermanagement.dto.requests;

import com.lab.backend.usermanagement.entity.Gender;
import com.lab.backend.usermanagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "First name must not be blank")
    private String firstName;
    @NotBlank(message = "Last name must not be blank")
    private String lastName;
    @NotBlank(message = "Username must not be blank")
    private String username;
    @Email(message = "It must be a valid email")
    @NotBlank(message = "Email must not be blank")
    private String email;
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must have at least 8 characters")
    private String password;
    @Size(min = 1, message = "User must have at least one role")
    @NotNull(message = "Role must not be null")
    private List<Role> roles;
    private Gender gender;
}
