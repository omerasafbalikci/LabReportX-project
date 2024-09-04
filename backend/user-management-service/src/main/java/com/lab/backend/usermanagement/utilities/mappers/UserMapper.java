package com.lab.backend.usermanagement.utilities.mappers;

import com.lab.backend.usermanagement.dao.RoleRepository;
import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.utilities.exceptions.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    @Autowired
    private RoleRepository roleRepository;

    public User toUser(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        Set<Role> roles = request.getRoles().stream()
                .map(this::mapRoleStringToRole)
                .collect(Collectors.toSet());
        user.setRoles(roles);

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
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()),
                user.getGender()
        );
    }

    private Role mapRoleStringToRole(String roleName) {
        return this.roleRepository.findByNameAndDeletedFalse(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));
    }
}
