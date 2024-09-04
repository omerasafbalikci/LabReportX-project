package com.lab.backend.usermanagement.dto.responses;

import com.lab.backend.usermanagement.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Set<String> roles;
    private Gender gender;
}
