package com.lab.backend.usermanagement.utilities.mappers;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toUser(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
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
                user.getEmail(),
                user.getRoles(),
                user.getGender()
        );
    }
}
