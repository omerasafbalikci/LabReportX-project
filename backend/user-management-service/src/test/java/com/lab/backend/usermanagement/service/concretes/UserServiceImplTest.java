package com.lab.backend.usermanagement.service.concretes;

import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.requests.UpdateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.repository.UserRepository;
import com.lab.backend.usermanagement.repository.UserSpecification;
import com.lab.backend.usermanagement.utilities.exceptions.RoleAlreadyExistsException;
import com.lab.backend.usermanagement.utilities.exceptions.SingleRoleRemovalException;
import com.lab.backend.usermanagement.utilities.exceptions.UserAlreadyExistsException;
import com.lab.backend.usermanagement.utilities.exceptions.UserNotFoundException;
import com.lab.backend.usermanagement.utilities.mappers.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setUsername("testUser");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userMapper.toGetUserResponse(user)).thenReturn(expectedResponse);

        // Act
        GetUserResponse actualResponse = userService.getUserById(userId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findByIdAndDeletedFalse(userId);
    }

    @Test
    void getUserById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findByIdAndDeletedFalse(userId);
    }

    @Test
    void getAllUsersFilteredAndSorted_shouldReturnPagedResponse() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "username";
        String direction = "ASC";
        User user = new User();
        user.setUsername("testUser");
        Page<User> userPage = new PageImpl<>(List.of(user));

        GetUserResponse userResponse = new GetUserResponse();
        userResponse.setUsername("testUser");

        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class)))
                .thenReturn(userPage);
        when(userMapper.toGetUserResponse(user)).thenReturn(userResponse);

        // Act
        PagedResponse<GetUserResponse> actualResponse = userService.getAllUsersFilteredAndSorted(
                page, size, sortBy, direction, null, null, null, null, null, null, null, null);

        // Assert
        assertEquals(1, actualResponse.getTotalItems());
        verify(userRepository, times(1)).findAll(any(UserSpecification.class), any(Pageable.class));
    }

    @Test
    void getUsernameByEmail_shouldReturnUsername_whenUserExists() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setUsername("testUser");

        when(userRepository.findByEmailAndDeletedFalse(email)).thenReturn(Optional.of(user));

        // Act
        String actualUsername = userService.getUsernameByEmail(email);

        // Assert
        assertEquals("testUser", actualUsername);
        verify(userRepository, times(1)).findByEmailAndDeletedFalse(email);
    }

    @Test
    void getUsernameByEmail_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmailAndDeletedFalse(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUsernameByEmail(email));
        verify(userRepository, times(1)).findByEmailAndDeletedFalse(email);
    }

    @Test
    void getCurrentUser_shouldReturnUserResponse_whenUserExists() {
        // Arrange
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setUsername(username);

        when(userRepository.findByUsernameAndDeletedFalse(username)).thenReturn(Optional.of(user));
        when(userMapper.toGetUserResponse(user)).thenReturn(expectedResponse);

        // Act
        GetUserResponse actualResponse = userService.getCurrentUser(username);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findByUsernameAndDeletedFalse(username);
    }

    @Test
    void getCurrentUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String username = "testUser";
        when(userRepository.findByUsernameAndDeletedFalse(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser(username));
        verify(userRepository, times(1)).findByUsernameAndDeletedFalse(username);
    }

    @Test
    void updateCurrentUser_shouldUpdateUserAndReturnResponse_whenDataIsValid() {
        // Arrange
        String username = "testUser";
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("UpdatedFirstName");

        User existingUser = new User();
        existingUser.setUsername(username);
        existingUser.setFirstName("OldFirstName");

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setUsername(username);
        expectedResponse.setFirstName("UpdatedFirstName");

        when(userRepository.findByUsernameAndDeletedFalse(username)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toGetUserResponse(existingUser)).thenReturn(expectedResponse);

        // Act
        GetUserResponse actualResponse = userService.updateCurrentUser(username, updateUserRequest);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository, times(1)).findByUsernameAndDeletedFalse(username);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateCurrentUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String username = "testUser";
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        when(userRepository.findByUsernameAndDeletedFalse(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateCurrentUser(username, updateUserRequest));
        verify(userRepository, times(1)).findByUsernameAndDeletedFalse(username);
    }

    @Test
    void createUser_whenUsernameExists_shouldThrowException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existingUser");

        when(userRepository.existsByUsernameAndDeletedIsFalse("existingUser")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(any(User.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    void createUser_whenEmailExists_shouldThrowException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existingEmail@example.com");

        when(userRepository.existsByEmailAndDeletedIsFalse("existingEmail@example.com")).thenReturn(true);

        // Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(any(User.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    void createUser_whenValid_shouldSaveUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newUser");
        request.setEmail("newUser@example.com");
        request.setRoles(List.of(Role.ADMIN));

        User mockUser = new User();
        mockUser.setUsername("newUser");
        mockUser.setEmail("newUser@example.com");

        when(userRepository.existsByUsernameAndDeletedIsFalse("newUser")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedIsFalse("newUser@example.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(mockUser);

        // Act
        userService.createUser(request);

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(1L);

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(request));
    }

    @Test
    void updateUser_whenIdIsNull_shouldThrowException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(null);

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(request));
    }

    @Test
    void updateUser_whenEmailExists_shouldThrowException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(1L);
        request.setEmail("newEmail@example.com");

        User existingUser = new User();
        existingUser.setEmail("existingEmail@example.com");

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndDeletedIsFalse("newEmail@example.com")).thenReturn(true);

        // Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUsernameExists_shouldThrowException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(1L);
        request.setUsername("newUsername");

        User existingUser = new User();
        existingUser.setUsername("existingUser");

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameAndDeletedIsFalse("newUsername")).thenReturn(true);

        // Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenValid_shouldSetDeletedFlag() {
        // Arrange
        User user = new User();
        user.setUsername("userToDelete");

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).save(user);
    }

    @Test
    void restoreUser_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        when(userRepository.findByIdAndDeletedTrue(1L)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.restoreUser(1L));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void restoreUser_whenValid_shouldUnsetDeletedFlag() {
        // Arrange
        User user = new User();
        user.setUsername("userToRestore");
        user.setDeleted(true);

        when(userRepository.findByIdAndDeletedTrue(1L)).thenReturn(Optional.of(user));

        userService.restoreUser(1L);

        // Assert
        assertFalse(user.isDeleted());
        verify(userRepository).save(user);
    }

    @Test
    void addRole_whenUserNotFound_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        Role role = Role.ADMIN;

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.addRole(userId, role));
    }

    @Test
    void addRole_whenUserAlreadyHasRole_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        Role role = Role.ADMIN;

        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setRoles(new ArrayList<>(Collections.singletonList(role)));

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(existingUser));

        // Assert
        assertThrows(RoleAlreadyExistsException.class, () -> userService.addRole(userId, role));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRole_whenValidRequest_shouldAddRole() {
        // Arrange
        Long userId = 1L;
        Role roleToAdd = Role.TECHNICIAN;

        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setRoles(new ArrayList<>(List.of(Role.ADMIN)));

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(existingUser));

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setUsername(existingUser.getUsername());
        when(userMapper.toGetUserResponse(existingUser)).thenReturn(expectedResponse);

        // Act
        GetUserResponse response = userService.addRole(userId, roleToAdd);

        // Assert
        assertTrue(existingUser.getRoles().contains(roleToAdd), "Role should be added to user");
        verify(userRepository).save(existingUser);
        assertEquals("existingUser", response.getUsername(), "Response username should match the existing user");
        assertEquals(2, existingUser.getRoles().size(), "User should have two roles now");
    }

    @Test
    void removeRole_whenUserNotFound_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        Role role = Role.ADMIN;

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.removeRole(userId, role));
    }

    @Test
    void removeRole_whenUserHasOnlyOneRole_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        Role role = Role.ADMIN;

        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setRoles(new ArrayList<>(Collections.singletonList(role)));

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(existingUser));

        // Assert
        assertThrows(SingleRoleRemovalException.class, () -> userService.removeRole(userId, role));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeRole_whenValidRequest_shouldRemoveRoleSuccessfully() {
        // Arrange
        Long userId = 1L;
        Role roleToRemove = Role.ADMIN;

        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setRoles(new ArrayList<>(Arrays.asList(Role.ADMIN, Role.TECHNICIAN)));

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(existingUser));

        GetUserResponse expectedResponse = new GetUserResponse();
        expectedResponse.setUsername(existingUser.getUsername());
        when(userMapper.toGetUserResponse(existingUser)).thenReturn(expectedResponse);

        // Act
        GetUserResponse response = userService.removeRole(userId, roleToRemove);

        // Assert
        assertFalse(existingUser.getRoles().contains(roleToRemove), "Role should be removed from user");
        verify(userRepository).save(existingUser);
        assertEquals("existingUser", response.getUsername(), "Response username should match the existing user");
        assertEquals(1, existingUser.getRoles().size(), "User should have only one role left");
    }
}
