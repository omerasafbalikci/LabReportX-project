package com.lab.backend.usermanagement.controller;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.requests.UpdateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.service.abstracts.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

    @Test
    public void testGetUserById() {
        // Arrange
        Long userId = 1L;
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.getUserById(userId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.getUserById(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    public void testGetAllUsersFilteredAndSorted() {
        // Arrange
        int page = 0;
        int size = 3;
        String sortBy = "id";
        String direction = "ASC";
        PagedResponse<GetUserResponse> expectedResponse = new PagedResponse<>();
        when(userService.getAllUsersFilteredAndSorted(page, size, sortBy, direction, null, null, null, null, null, null, null, null))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagedResponse<GetUserResponse>> responseEntity = userController.getAllUsersFilteredAndSorted(page, size, sortBy, direction, null, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).getAllUsersFilteredAndSorted(page, size, sortBy, direction, null, null, null, null, null, null, null, null);
    }

    @Test
    public void testGetUsernameByEmail() {
        // Arrange
        String email = "test@example.com";
        String expectedUsername = "testUser";
        when(userService.getUsernameByEmail(email)).thenReturn(expectedUsername);

        // Act
        String username = userController.getUsernameByEmail(email);

        // Assert
        assertEquals(expectedUsername, username);
        verify(userService, times(1)).getUsernameByEmail(email);
    }

    @Test
    public void testGetCurrentUser() {
        // Arrange
        String username = "testUser";
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.getCurrentUser(username)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.getCurrentUser(username);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).getCurrentUser(username);
    }

    @Test
    void testUpdateCurrentUser() {
        // Arrange
        String username = "testUser";
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("UpdatedFirstName");
        updateUserRequest.setLastName("UpdatedLastName");

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setUsername(username);
        expectedResponse.setFirstName("UpdatedFirstName");
        expectedResponse.setLastName("UpdatedLastName");

        when(userService.updateCurrentUser(username, updateUserRequest)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.updateCurrentUser(username, updateUserRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());

        verify(userService, times(1)).updateCurrentUser(username, updateUserRequest);
    }

    @Test
    public void testCreateUser() {
        // Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest();
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.createUser(createUserRequest)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.createUser(createUserRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).createUser(createUserRequest);
    }

    @Test
    public void testUpdateUser() {
        // Arrange
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.updateUser(updateUserRequest)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.updateUser(updateUserRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).updateUser(updateUserRequest);
    }

    @Test
    public void testDeleteUser() {
        // Arrange
        Long userId = 1L;

        // Act
        ResponseEntity<String> responseEntity = userController.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User has been successfully deleted.", responseEntity.getBody());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    public void testRestoreUser() {
        // Arrange
        Long userId = 1L;
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.restoreUser(userId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.restoreUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).restoreUser(userId);
    }

    @Test
    public void testAddRole() {
        // Arrange
        Long userId = 1L;
        Role role = Role.ADMIN;
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.addRole(userId, role)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.addRole(userId, role);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).addRole(userId, role);
    }

    @Test
    public void testRemoveRole() {
        // Arrange
        Long userId = 1L;
        Role role = Role.ADMIN;
        GetUserResponse expectedResponse = new GetUserResponse();
        when(userService.removeRole(userId, role)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetUserResponse> responseEntity = userController.removeRole(userId, role);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(userService, times(1)).removeRole(userId, role);
    }
}
