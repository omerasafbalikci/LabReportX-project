package com.lab.backend.usermanagement.service.concretes;

import com.lab.backend.usermanagement.dto.requests.*;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.repository.UserRepository;
import com.lab.backend.usermanagement.repository.UserSpecification;
import com.lab.backend.usermanagement.service.abstracts.UserService;
import com.lab.backend.usermanagement.utilities.exceptions.*;
import com.lab.backend.usermanagement.utilities.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface for managing user-related operations.
 * Provides methods for user retrieval, creation, updating, deletion,
 * and role management, along with interaction with RabbitMQ for asynchronous processing.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routingKey.create}")
    private String ROUTING_KEY_CREATE;

    @Value("${rabbitmq.routingKey.update}")
    private String ROUTING_KEY_UPDATE;

    @Value("${rabbitmq.routingKey.delete}")
    private String ROUTING_KEY_DELETE;

    @Value("${rabbitmq.routingKey.restore}")
    private String ROUTING_KEY_RESTORE;

    @Value("${rabbitmq.routingKey.addRole}")
    private String ROUTING_KEY_ADD_ROLE;

    @Value("${rabbitmq.routingKey.removeRole}")
    private String ROUTING_KEY_REMOVE_ROLE;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;
    private static final String FAIL_RABBITMQ = "Failed to send update message to RabbitMQ";
    private static final String USER_DOES_NOT_EXIST = "User doesn't exist with id ";

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to be fetched
     * @return a response containing the user details
     * @throws UserNotFoundException if no user is found with the given ID
     */
    @Override
    public GetUserResponse getUserById(Long id) {
        log.trace("Entering getUserById method in UserServiceImpl");
        log.info("Fetching user by ID: {}", id);
        User user = this.userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("User not found with ID: {}", id);
            return new UserNotFoundException("User not found with id: " + id);
        });
        GetUserResponse response = this.userMapper.toGetUserResponse(user);
        log.debug("Fetched user: {}", response);
        log.trace("Exiting getUserById method in UserServiceImpl");
        return response;
    }

    /**
     * Retrieves all users filtered and sorted based on the provided parameters.
     *
     * @param page       the page number to retrieve
     * @param size       the number of users per page
     * @param sortBy     the attribute to sort by
     * @param direction  the direction of sorting (asc or desc)
     * @param firstName  the first name filter
     * @param lastName   the last name filter
     * @param username   the username filter
     * @param hospitalId the hospital ID filter
     * @param email      the email filter
     * @param role       the role filter
     * @param gender     the gender filter
     * @param deleted    whether to include deleted users
     * @return a paginated response containing the filtered and sorted user details
     */
    @Override
    public PagedResponse<GetUserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName, String lastName,
                                                                       String username, String hospitalId, String email, String role, String gender, Boolean deleted) {
        log.trace("Entering getAllUsersFilteredAndSorted method in UserServiceImpl");
        log.info("Fetching users with filters: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        UserSpecification specification = new UserSpecification(firstName, lastName, username, hospitalId, email, role, gender, deleted);
        Page<User> userPage = this.userRepository.findAll(specification, pagingSort);
        List<GetUserResponse> userResponses = userPage.getContent()
                .stream()
                .map(this.userMapper::toGetUserResponse)
                .toList();
        log.debug("Fetched {} users", userResponses.size());
        log.trace("Exiting getAllUsersFilteredAndSorted method in UserServiceImpl");
        return new PagedResponse<>(
                userResponses,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userPage.isFirst(),
                userPage.isLast(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    /**
     * Retrieves the username associated with the given email address.
     *
     * @param email the email address of the user
     * @return the username associated with the email address
     * @throws UserNotFoundException if no user is found registered to the given email address
     */
    @Override
    public String getUsernameByEmail(String email) {
        log.trace("Entering getUsernameByEmail method in UserServiceImpl");
        log.info("Fetching username by email: {}", email);
        Optional<User> optionalUser = this.userRepository.findByEmailAndDeletedFalse(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            log.debug("Found username: {}", user.getUsername());
            log.trace("Exiting getUsernameByEmail method in UserServiceImpl");
            return user.getUsername();
        } else {
            log.error("No users registered to this email address: {}", email);
            throw new UserNotFoundException("No users registered to this email address were found: " + email);
        }
    }

    /**
     * Retrieves the current user by their username.
     *
     * @param username the username of the current user
     * @return a response containing the current user's details
     * @throws UserNotFoundException if no user is found with the given username
     */
    @Override
    public GetUserResponse getCurrentUser(String username) {
        log.trace("Entering getCurrentUser method in UserServiceImpl");
        log.info("Fetching current user by username: {}", username);
        User user = this.userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(() -> {
            log.error("User not found with username: {}", username);
            return new UserNotFoundException("User not found with username: " + username);
        });
        GetUserResponse response = this.userMapper.toGetUserResponse(user);
        log.debug("Fetched current user: {}", response);
        log.trace("Exiting getCurrentUser method in UserServiceImpl");
        return response;
    }

    /**
     * Updates the current user based on the provided username and update request.
     *
     * @param username          the username of the user to update
     * @param updateUserRequest the request containing the updated user information
     * @return the updated user response
     * @throws UserNotFoundException      if the user with the specified username does not exist
     * @throws UserAlreadyExistsException if the provided email or username is already taken
     * @throws RabbitMQException          if the message sending to RabbitMQ fails
     */
    @Override
    @Transactional
    public GetUserResponse updateCurrentUser(String username, UpdateUserRequest updateUserRequest) {
        log.trace("Entering updateCurrentUser method in UserServiceImpl");
        log.info("Updating current user: {}", username);
        User existingUser = this.userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> {
                    log.error("User doesn't exist with username: {}", username);
                    return new UserNotFoundException("User doesn't exist with username " + username);
                });

        String existingUsername = existingUser.getUsername();

        if (updateUserRequest.getEmail() != null && !existingUser.getEmail().equals(updateUserRequest.getEmail())) {
            if (this.userRepository.existsByEmailAndDeletedIsFalse(updateUserRequest.getEmail())) {
                log.error("Email is already taken: {}", updateUserRequest.getEmail());
                throw new UserAlreadyExistsException("Email is already taken");
            }
            existingUser.setEmail(updateUserRequest.getEmail());
            log.debug("Updated email for user: {}", existingUser);
        }
        if (updateUserRequest.getFirstName() != null && !existingUser.getFirstName().equals(updateUserRequest.getFirstName())) {
            existingUser.setFirstName(updateUserRequest.getFirstName());
            log.debug("Updated first name for user: {}", existingUser);
        }
        if (updateUserRequest.getLastName() != null && !existingUser.getLastName().equals(updateUserRequest.getLastName())) {
            existingUser.setLastName(updateUserRequest.getLastName());
            log.debug("Updated last name for user: {}", existingUser);
        }
        if (updateUserRequest.getGender() != null && !existingUser.getGender().equals(updateUserRequest.getGender())) {
            existingUser.setGender(updateUserRequest.getGender());
            log.debug("Updated gender for user: {}", existingUser);
        }
        if (updateUserRequest.getUsername() != null && !existingUser.getUsername().equals(updateUserRequest.getUsername())) {
            if (this.userRepository.existsByUsernameAndDeletedIsFalse(updateUserRequest.getUsername())) {
                log.error("Username is taken: {}", updateUserRequest.getUsername());
                throw new UserAlreadyExistsException("Username is taken");
            }
            existingUser.setUsername(updateUserRequest.getUsername());

            try {
                UpdateAuthUserRequest updateAuthUserRequest = new UpdateAuthUserRequest(existingUsername, updateUserRequest.getUsername());
                this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_UPDATE, updateAuthUserRequest);
                log.info("Sent update message to RabbitMQ for user: {}", existingUser);
            } catch (Exception e) {
                log.error("Failed to send update message to RabbitMQ: {}", e.getMessage());
                throw new RabbitMQException(FAIL_RABBITMQ, e);
            }
        }
        this.userRepository.save(existingUser);
        log.debug("Saved updated user: {}", existingUser);
        log.trace("Exiting updateCurrentUser method in UserServiceImpl");
        return this.userMapper.toGetUserResponse(existingUser);
    }

    /**
     * Creates a new user based on the provided user creation request.
     *
     * @param createUserRequest the request containing the new user information
     * @return the created user response
     * @throws UserAlreadyExistsException if the provided username or email is already taken
     * @throws RabbitMQException          if the message sending to RabbitMQ fails
     */
    @Override
    @Transactional
    public GetUserResponse createUser(CreateUserRequest createUserRequest) {
        log.trace("Entering createUser method in UserServiceImpl");
        log.debug("Creating user with username: {}", createUserRequest.getUsername());
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(createUserRequest.getUsername())) {
            log.error("Username '{}' is already taken", createUserRequest.getUsername());
            throw new UserAlreadyExistsException("Username '" + createUserRequest.getUsername() + "' is already taken");
        }
        if (this.userRepository.existsByEmailAndDeletedIsFalse(createUserRequest.getEmail())) {
            log.error("Email '{}' is already taken", createUserRequest.getEmail());
            throw new UserAlreadyExistsException("Email '" + createUserRequest.getEmail() + "' is already taken");
        }
        User user = this.userMapper.toUser(createUserRequest);
        List<String> roles = createUserRequest.getRoles().stream().map(Enum::toString).collect(Collectors.toList());

        try {
            CreateAuthUserRequest createAuthUserRequest = new CreateAuthUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword(), createUserRequest.getEmail(), roles);
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREATE, createAuthUserRequest);
            log.info("Sent create message to RabbitMQ for user: {}", createUserRequest.getUsername());
        } catch (Exception exception) {
            log.error("Failed to send create message to RabbitMQ", exception);
            throw new RabbitMQException("Failed to send create message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        log.info("User created successfully: {}", user.getUsername());
        log.trace("Exiting createUser method in UserServiceImpl");
        return this.userMapper.toGetUserResponse(user);
    }

    /**
     * Updates a user based on the provided update request.
     *
     * @param updateUserRequest the request containing the updated user information
     * @return the updated user response
     * @throws UserNotFoundException      if the user with the specified ID does not exist
     * @throws UserAlreadyExistsException if the provided email or username is already taken
     * @throws RabbitMQException          if the message sending to RabbitMQ fails
     */
    @Override
    @Transactional
    public GetUserResponse updateUser(UpdateUserRequest updateUserRequest) {
        log.trace("Entering updateUser method in UserServiceImpl");
        log.debug("Updating user with id: {}", updateUserRequest.getId());
        if (updateUserRequest.getId() == null) {
            log.error("Id must not be null");
            throw new UserNotFoundException("Id must not be null");
        }
        User existingUser = this.userRepository.findByIdAndDeletedFalse(updateUserRequest.getId())
                .orElseThrow(() -> {
                    log.error("User doesn't exist with id {}", updateUserRequest.getId());
                    return new UserNotFoundException(USER_DOES_NOT_EXIST + updateUserRequest.getId());
                });

        String existingUsername = existingUser.getUsername();

        if (updateUserRequest.getEmail() != null && !existingUser.getEmail().equals(updateUserRequest.getEmail())) {
            if (this.userRepository.existsByEmailAndDeletedIsFalse(updateUserRequest.getEmail())) {
                log.error("Email is already taken {}", updateUserRequest.getEmail());
                throw new UserAlreadyExistsException("Email is already taken");
            }
            existingUser.setEmail(updateUserRequest.getEmail());
        }
        if (updateUserRequest.getFirstName() != null && !existingUser.getFirstName().equals(updateUserRequest.getFirstName())) {
            existingUser.setFirstName(updateUserRequest.getFirstName());
        }
        if (updateUserRequest.getLastName() != null && !existingUser.getLastName().equals(updateUserRequest.getLastName())) {
            existingUser.setLastName(updateUserRequest.getLastName());
        }
        if (updateUserRequest.getGender() != null && !existingUser.getGender().equals(updateUserRequest.getGender())) {
            existingUser.setGender(updateUserRequest.getGender());
        }
        if (updateUserRequest.getUsername() != null && !existingUser.getUsername().equals(updateUserRequest.getUsername())) {
            if (this.userRepository.existsByUsernameAndDeletedIsFalse(updateUserRequest.getUsername())) {
                log.error("Username is taken {}", updateUserRequest.getUsername());
                throw new UserAlreadyExistsException("Username is taken");
            }
            existingUser.setUsername(updateUserRequest.getUsername());

            try {
                UpdateAuthUserRequest updateAuthUserRequest = new UpdateAuthUserRequest(existingUsername, updateUserRequest.getUsername());
                this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_UPDATE, updateAuthUserRequest);
                log.info("Sent update message to RabbitMQ for user {}", existingUser.getUsername());
            } catch (Exception e) {
                log.error(FAIL_RABBITMQ, e);
                throw new RabbitMQException(FAIL_RABBITMQ, e);
            }
        }
        this.userRepository.save(existingUser);
        log.info("User updated successfully: {}", existingUser.getUsername());
        log.trace("Exiting updateUser method in UserServiceImpl");
        return this.userMapper.toGetUserResponse(existingUser);
    }

    /**
     * Deletes a user by setting their deleted status to true.
     *
     * @param id the ID of the user to delete
     * @throws UserNotFoundException if the user with the specified ID does not exist
     * @throws RabbitMQException     if the message sending to RabbitMQ fails
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.trace("Entering deleteUser method in UserServiceImpl");
        log.debug("Deleting user with id: {}", id);
        User user = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.error("User doesn't exist with id: {}", id);
                    return new UserNotFoundException(USER_DOES_NOT_EXIST + id);
                });
        String username = user.getUsername();
        user.setDeleted(true);
        try {
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_DELETE, username);
            log.info("Sent delete message to RabbitMQ for user: {}", username);
        } catch (Exception exception) {
            log.error("Failed to send delete message to RabbitMQ", exception);
            throw new RabbitMQException("Failed to send delete message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        log.info("User deleted successfully: {}", username);
        log.trace("Exiting deleteUser method in UserServiceImpl");
    }

    /**
     * Restores a previously deleted user by setting their deleted status to false.
     *
     * @param id the ID of the user to restore
     * @throws UserNotFoundException if the user with the specified ID does not exist
     */
    @Override
    @Transactional
    public GetUserResponse restoreUser(Long id) {
        log.trace("Entering restoreUser method in UserServiceImpl");
        log.debug("Restoring user with id: {}", id);
        User user = this.userRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> {
                    log.error("User does not exist with id {}", id);
                    return new UserNotFoundException(USER_DOES_NOT_EXIST + id);
                });
        String username = user.getUsername();
        user.setDeleted(false);
        try {
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_RESTORE, username);
            log.info("Sent restore message to RabbitMQ for user: {}", username);
        } catch (Exception exception) {
            log.error("Failed to send restore message to RabbitMQ", exception);
            throw new RabbitMQException("Failed to send restore message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        log.info("User restored successfully: {}", username);
        log.trace("Exiting restoreUser method in UserServiceImpl");
        return this.userMapper.toGetUserResponse(user);
    }

    /**
     * Adds a specified role to a user.
     *
     * @param id   the ID of the user to whom the role will be added
     * @param role the role to be added to the user
     * @return GetUserResponse containing the updated user information
     * @throws UserNotFoundException      if the user with the specified ID does not exist
     * @throws RoleAlreadyExistsException if the user already has the specified role
     * @throws RabbitMQException          if there is an error sending the role addition message to RabbitMQ
     */
    @Override
    @Transactional
    public GetUserResponse addRole(Long id, Role role) {
        log.trace("Entering addRole method in UserServiceImpl");
        log.info("Adding role '{}' to user with id '{}'", role, id);
        User user = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.error("User not found with id '{}'", id);
                    return new UserNotFoundException(USER_DOES_NOT_EXIST + id);
                });
        String username = user.getUsername();
        if (user.getRoles().contains(role)) {
            log.error("User '{}' already has this role: '{}'", username, role);
            throw new RoleAlreadyExistsException("User already has this Role. Role: " + role.toString());
        }
        user.getRoles().add(role);
        try {
            String r = role.toString();
            UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, r);
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_ADD_ROLE, updateAuthUserRoleRequest);
            log.info("Successfully sent add role message for user '{}'", username);
        } catch (Exception exception) {
            log.error("Failed to send add role message to RabbitMQ for user '{}'", username, exception);
            throw new RabbitMQException("Failed to send add role message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        log.info("Role '{}' added successfully to user '{}'", role, username);
        log.trace("Exiting addRole method in UserServiceImpl");
        return this.userMapper.toGetUserResponse(user);
    }

    /**
     * Removes a specified role from a user.
     *
     * @param id   the ID of the user from whom the role will be removed
     * @param role the role to be removed from the user
     * @return GetUserResponse containing the updated user information
     * @throws UserNotFoundException      if the user with the specified ID does not exist
     * @throws SingleRoleRemovalException if the user has only one role remaining
     * @throws RoleNotFoundException      if the user does not own the specified role
     * @throws RabbitMQException          if there is an error sending the role removal message to RabbitMQ
     */
    @Override
    @Transactional
    public GetUserResponse removeRole(Long id, Role role) {
        log.trace("Entering removeRole method in UserServiceImpl");
        log.info("Removing role '{}' from user with id '{}'", role, id);
        User user = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: '{}'", id);
                    return new UserNotFoundException(USER_DOES_NOT_EXIST + id);
                });
        String username = user.getUsername();
        if (user.getRoles().size() <= 1) {
            log.error("Cannot remove role. User '{}' must have at least one role", username);
            throw new SingleRoleRemovalException("Cannot remove role. User must have at least one role");
        }
        if (!user.getRoles().contains(role)) {
            log.error("The user '{}' does not own this role: '{}'", username, role);
            throw new RoleNotFoundException("The user does not own this role! Role: " + role);
        }
        user.getRoles().remove(role);
        try {
            String r = role.toString();
            UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, r);
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_REMOVE_ROLE, updateAuthUserRoleRequest);
            log.info("Successfully sent remove role message for user '{}'", username);
        } catch (Exception exception) {
            log.error("Failed to send remove role message to RabbitMQ for user '{}'", username, exception);
            throw new RabbitMQException("Failed to send remove role message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        log.info("Role '{}' removed successfully from user '{}'", role, username);
        log.trace("Exiting removeRole method in UserServiceImpl");
        return this.userMapper.toGetUserResponse(user);
    }
}

