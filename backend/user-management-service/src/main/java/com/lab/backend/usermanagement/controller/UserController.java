package com.lab.backend.usermanagement.controller;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.requests.UpdateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.service.abstracts.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 * Provides endpoints to create, update, delete, restore users, as well as to retrieve user details and manage user roles.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve.
     * @return ResponseEntity containing the user details.
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<GetUserResponse> getUserById(@PathVariable Long id) {
        log.info("Fetching user by id: {}", id);
        GetUserResponse response = this.userService.getUserById(id);
        log.info("User with id {} retrieved successfully", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated, filtered, and sorted list of users.
     *
     * @param page       the page number (default is 0).
     * @param size       the page size (default is 3).
     * @param sortBy     the field to sort by (default is id).
     * @param direction  the sort direction (ASC or DESC, default is ASC).
     * @param firstName  optional filter for user's first name.
     * @param lastName   optional filter for user's last name.
     * @param username   optional filter for user's username.
     * @param hospitalId optional filter for user's hospital ID.
     * @param email      optional filter for user's email.
     * @param role       optional filter for user's role.
     * @param gender     optional filter for user's gender.
     * @param deleted    optional filter for user's deletion status.
     * @return ResponseEntity containing the paginated and filtered list of users.
     */
    @GetMapping("/filtered-and-sorted")
    public ResponseEntity<PagedResponse<GetUserResponse>> getAllUsersFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Boolean deleted
    ) {
        log.info("Fetching users with filters - page: {}, size: {}, sortBy: {}, direction: {}, firstName: {}, lastName: {}, username: {}, hospitalId: {}, email: {}, role: {}, gender: {}, deleted: {}",
                page, size, sortBy, direction, firstName, lastName, username, hospitalId, email, role, gender, deleted);
        PagedResponse<GetUserResponse> response = this.userService.getAllUsersFilteredAndSorted(page, size, sortBy, direction, firstName, lastName, username, hospitalId, email, role, gender, deleted);
        log.info("Users filtered and sorted fetched successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the username associated with the given email.
     *
     * @param email the email of the user to retrieve the username.
     * @return the username of the user with the given email.
     */
    @GetMapping("/email")
    public String getUsernameByEmail(@RequestParam String email) {
        log.info("Fetching username by email: {}", email);
        return this.userService.getUsernameByEmail(email);
    }

    /**
     * Retrieves the details of the current user based on their username.
     *
     * @param username the username of the current user.
     * @return ResponseEntity containing the user details.
     */
    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getCurrentUser(@RequestHeader("X-Username") String username) {
        log.info("Fetching current user by username: {}", username);
        GetUserResponse response = this.userService.getCurrentUser(username);
        log.info("Current user retrieved successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the current user's details.
     *
     * @param username          the username of the current user.
     * @param updateUserRequest the request containing the updated user details.
     * @return ResponseEntity containing the updated user details.
     */
    @PutMapping("/update/me")
    public ResponseEntity<GetUserResponse> updateCurrentUser(@RequestHeader("X-Username") String username, @RequestBody @Valid UpdateUserRequest updateUserRequest) {
        log.info("Updating current user with username: {}", username);
        GetUserResponse response = this.userService.updateCurrentUser(username, updateUserRequest);
        log.info("Current user updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new user.
     *
     * @param createUserRequest the request containing the user details.
     * @return ResponseEntity containing the created user details.
     */
    @PostMapping
    public ResponseEntity<GetUserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        log.info("Creating a new user with details: {}", createUserRequest);
        GetUserResponse response = this.userService.createUser(createUserRequest);
        log.info("User created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing user.
     *
     * @param updateUserRequest the request containing the updated user details.
     * @return ResponseEntity containing the updated user details.
     */
    @PutMapping
    public ResponseEntity<GetUserResponse> updateUser(@RequestBody @Valid UpdateUserRequest updateUserRequest) {
        log.info("Updating user with details: {}", updateUserRequest);
        GetUserResponse response = this.userService.updateUser(updateUserRequest);
        log.info("User updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete.
     * @return ResponseEntity confirming the deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        this.userService.deleteUser(id);
        log.info("User with id {} deleted successfully", id);
        return ResponseEntity.ok("User has been successfully deleted.");
    }

    /**
     * Restores a deleted user by their ID.
     *
     * @param id the ID of the user to restore.
     * @return ResponseEntity containing the restored user details.
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<GetUserResponse> restoreUser(@PathVariable Long id) {
        log.info("Restoring user with id: {}", id);
        GetUserResponse response = this.userService.restoreUser(id);
        log.info("User with id {} restored successfully", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Adds a role to the specified user.
     *
     * @param id   the ID of the user.
     * @param role the role to add to the user.
     * @return ResponseEntity containing the updated user details.
     */
    @PutMapping("/role/add/{userId}")
    public ResponseEntity<GetUserResponse> addRole(@PathVariable("userId") Long id, @RequestBody Role role) {
        log.info("Adding role to user with id: {}", id);
        GetUserResponse response = this.userService.addRole(id, role);
        log.info("Role added to user with id {} successfully", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Removes a role from the specified user.
     *
     * @param id   the ID of the user.
     * @param role the role to remove from the user.
     * @return ResponseEntity containing the updated user details.
     */
    @PutMapping("/role/remove/{userId}")
    public ResponseEntity<GetUserResponse> removeRole(@PathVariable("userId") Long id, @RequestBody Role role) {
        log.info("Removing role from user with id: {}", id);
        GetUserResponse response = this.userService.removeRole(id, role);
        log.info("Role removed from user with id {} successfully", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
