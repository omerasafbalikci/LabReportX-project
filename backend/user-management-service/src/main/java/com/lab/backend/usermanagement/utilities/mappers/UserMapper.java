package com.lab.backend.usermanagement.utilities.mappers;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.utilities.HospitalIdGenerator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {
    @Autowired
    private HospitalIdGenerator hospitalIdGenerator;

    public User toUser(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setHospitalId(this.hospitalIdGenerator.generateUniqueHospitalId());
        user.setEmail(request.getEmail());
        user.setRoles(request.getRoles());
        user.setGender(request.getGender());
        return user;
    }

    public GetUserResponse toGetUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new GetUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getHospitalId(),
                user.getEmail(),
                user.getRoles(),
                user.getGender()
        );
    }
}
