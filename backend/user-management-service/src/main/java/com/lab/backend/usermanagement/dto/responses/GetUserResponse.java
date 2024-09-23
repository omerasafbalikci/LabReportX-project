package com.lab.backend.usermanagement.dto.responses;

import com.lab.backend.usermanagement.entity.Gender;
import com.lab.backend.usermanagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String hospitalId;
    private String email;
    private List<String> roles;
    private Gender gender;
}
