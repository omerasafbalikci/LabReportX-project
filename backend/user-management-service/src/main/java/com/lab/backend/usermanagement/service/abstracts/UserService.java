package com.lab.backend.usermanagement.service.abstracts;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.requests.UpdateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;

/**
 * Service interface for managing user-related operations.
 * Provides methods for user retrieval, creation, updating, deletion,
 * and role management.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface UserService {
    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return a {@link GetUserResponse} containing user details
     */
    GetUserResponse getUserById(Long id);

    /**
     * Retrieves all users, with optional filtering and sorting.
     *
     * @param page       the page number to retrieve
     * @param size       the number of users per page
     * @param sortBy     the field to sort by
     * @param direction  the direction of sorting (e.g., ascending or descending)
     * @param firstName  filter by first name
     * @param lastName   filter by last name
     * @param username   filter by username
     * @param hospitalId filter by hospital ID
     * @param email      filter by email address
     * @param role       filter by user role
     * @param gender     filter by gender
     * @param deleted    filter by deleted status
     * @return a {@link PagedResponse} containing a list of {@link GetUserResponse}
     */
    PagedResponse<GetUserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName, String lastName,
                                                                String username, String hospitalId, String email, String role, String gender, Boolean deleted);

    /**
     * Retrieves the username associated with a given email.
     *
     * @param email the email to search for
     * @return the username corresponding to the provided email
     */
    String getUsernameByEmail(String email);

    /**
     * Retrieves the current user's details by username.
     *
     * @param username the username of the current user
     * @return a {@link GetUserResponse} containing the current user's details
     */
    GetUserResponse getCurrentUser(String username);

    /**
     * Updates the current user's details.
     *
     * @param username          the username of the current user
     * @param updateUserRequest the updated user information
     * @return a {@link GetUserResponse} containing the updated user details
     */
    GetUserResponse updateCurrentUser(String username, UpdateUserRequest updateUserRequest);

    /**
     * Creates a new user.
     *
     * @param createUserRequest the information needed to create the user
     * @return a {@link GetUserResponse} containing the newly created user's details
     */
    GetUserResponse createUser(CreateUserRequest createUserRequest);

    /**
     * Updates an existing user.
     *
     * @param updateUserRequest the updated user information
     * @return a {@link GetUserResponse} containing the updated user details
     */
    GetUserResponse updateUser(UpdateUserRequest updateUserRequest);

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     */
    void deleteUser(Long id);

    /**
     * Restores a deleted user by their ID.
     *
     * @param id the ID of the user to restore
     * @return a {@link GetUserResponse} containing the restored user's details
     */
    GetUserResponse restoreUser(Long id);

    /**
     * Adds a role to a user.
     *
     * @param id   the ID of the user to whom the role will be added
     * @param role the role to add
     * @return a {@link GetUserResponse} containing the user's updated details
     */
    GetUserResponse addRole(Long id, Role role);

    /**
     * Removes a role from a user.
     *
     * @param id   the ID of the user from whom the role will be removed
     * @param role the role to remove
     * @return a {@link GetUserResponse} containing the user's updated details
     */
    GetUserResponse removeRole(Long id, Role role);
}
