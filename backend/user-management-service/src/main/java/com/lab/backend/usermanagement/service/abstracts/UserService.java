package com.lab.backend.usermanagement.service.abstracts;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.requests.UpdateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;

public interface UserService {
    GetUserResponse getUserById(Long id);

    PagedResponse<GetUserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                String lastName, String username, String email, String role, String gender,
                                                                Boolean deleted);

    GetUserResponse createUser(CreateUserRequest createUserRequest);

    GetUserResponse updateUser(UpdateUserRequest updateUserRequest);

    void deleteUser(Long id);

    GetUserResponse restoreUser(Long id);

    GetUserResponse addRole(Long id, Role role);

    GetUserResponse removeRole(Long id, Role role);
}
