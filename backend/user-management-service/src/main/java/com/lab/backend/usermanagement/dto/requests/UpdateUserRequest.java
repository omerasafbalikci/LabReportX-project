package com.lab.backend.usermanagement.dto.requests;

import com.lab.backend.usermanagement.entity.Gender;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to update user.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    @Email(message = "It must be a valid email")
    private String email;
    private Gender gender;
}
