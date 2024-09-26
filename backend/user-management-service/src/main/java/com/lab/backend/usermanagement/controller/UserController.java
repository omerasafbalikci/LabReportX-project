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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;

    @GetMapping("/id/{id}")
    public ResponseEntity<GetUserResponse> getUserById(@PathVariable Long id) {
        GetUserResponse response = this.userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

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
        PagedResponse<GetUserResponse> response = this.userService.getAllUsersFilteredAndSorted(page, size, sortBy, direction, firstName, lastName, username, hospitalId, email, role, gender, deleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email")
    public String getUsernameByEmail(@RequestParam String email) {
        return this.userService.getUsernameByEmail(email);
    }
    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getCurrentUser(@RequestHeader("X-Username") String username) {
        GetUserResponse response = this.userService.getCurrentUser(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/me")
    public ResponseEntity<GetUserResponse> updateCurrentUser(@RequestHeader("X-Username") String username, @RequestBody @Valid UpdateUserRequest updateUserRequest) {
        GetUserResponse response = this.userService.updateCurrentUser(username, updateUserRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GetUserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        GetUserResponse response = this.userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<GetUserResponse> updateUser(@RequestBody @Valid UpdateUserRequest updateUserRequest) {
        GetUserResponse response = this.userService.updateUser(updateUserRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.ok("User has been successfully deleted.");
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<GetUserResponse> restoreUser(@PathVariable Long id) {
        GetUserResponse response = this.userService.restoreUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/role/add/{userId}")
    public ResponseEntity<GetUserResponse> addRole(@PathVariable("userId") Long id, @RequestBody Role role) {
        GetUserResponse response = this.userService.addRole(id, role);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/role/remove/{userId}")
    public ResponseEntity<GetUserResponse> removeRole(@PathVariable("userId") Long id, @RequestBody Role role) {
        GetUserResponse response = this.userService.removeRole(id, role);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
