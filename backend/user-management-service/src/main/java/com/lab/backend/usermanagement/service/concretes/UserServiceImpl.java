package com.lab.backend.usermanagement.service.concretes;

import com.lab.backend.usermanagement.repository.UserRepository;
import com.lab.backend.usermanagement.repository.UserSpecification;
import com.lab.backend.usermanagement.dto.requests.*;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
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
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public GetUserResponse getUserById(Long id) {
        log.trace("Fetching user by ID: {}", id);
        User user = this.userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("User not found with id: {}", id);
            return new UserNotFoundException("User not found with id: " + id);
        });
        GetUserResponse response = this.userMapper.toGetUserResponse(user);
        log.info("Successfully fetched user by ID: {}", id);
        return response;
    }

    @Override
    public PagedResponse<GetUserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                       String lastName, String username, String hospitalId, String email, String role, String gender,
                                                                       Boolean deleted) {
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        UserSpecification specification = new UserSpecification(firstName, lastName, username, hospitalId, email, role, gender, deleted);
        Page<User> userPage = this.userRepository.findAll(specification, pagingSort);
        List<GetUserResponse> userResponses = userPage.getContent()
                .stream()
                .map(this.userMapper::toGetUserResponse)
                .toList();

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

    @Override
    @Transactional
    public GetUserResponse createUser(CreateUserRequest createUserRequest) {
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(createUserRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + createUserRequest.getUsername() + "' is already taken");
        }
        if (this.userRepository.existsByEmailAndDeletedIsFalse(createUserRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email '" + createUserRequest.getEmail() + "' is already taken");
        }
        User user = this.userMapper.toUser(createUserRequest);
        Set<String> roles = createUserRequest.getRoles().stream().map(Enum::toString).collect(Collectors.toSet());

        try {
            CreateAuthUserRequest createAuthUserRequest = new CreateAuthUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword(), roles);
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREATE, createAuthUserRequest);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send create message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        return this.userMapper.toGetUserResponse(user);
    }

    @Override
    @Transactional
    public GetUserResponse updateUser(UpdateUserRequest updateUserRequest) {
        User existingUser = this.userRepository.findByIdAndDeletedFalse(updateUserRequest.getId())
                .orElseThrow(() -> {
                    return new UserNotFoundException("User doesn't exist with id " + updateUserRequest.getId());
                });

        if (updateUserRequest.getUsername() != null && !existingUser.getUsername().equals(updateUserRequest.getUsername())) {
            if (this.userRepository.existsByUsernameAndDeletedIsFalse(updateUserRequest.getUsername())) {
                throw new UserAlreadyExistsException("Username is taken");
            }
            existingUser.setUsername(updateUserRequest.getUsername());
        }
        if (updateUserRequest.getEmail() != null && !existingUser.getEmail().equals(updateUserRequest.getEmail())) {
            if (this.userRepository.existsByEmailAndDeletedIsFalse(updateUserRequest.getEmail())) {
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

        try {
            UpdateAuthUserRequest updateAuthUserRequest = new UpdateAuthUserRequest(updateUserRequest.getId(), updateUserRequest.getUsername());
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_UPDATE, updateAuthUserRequest);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send update message to RabbitMQ", exception);
        }
        this.userRepository.save(existingUser);
        return this.userMapper.toGetUserResponse(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User doesn't exist with id " + id);
                });
        user.setDeleted(true);
        try {
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_DELETE, id);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send delete message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public GetUserResponse restoreUser(Long id) {
        User user = this.userRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User doesn't exist with id " + id);
                });
        user.setDeleted(false);
        try {
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_RESTORE, id);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send restore message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        return this.userMapper.toGetUserResponse(user);
    }

    @Override
    @Transactional
    public GetUserResponse addRole(Long id, Role role) {
        User user = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User doesn't exist with id " + id);
                });
        if (user.getRoles().contains(role)) {
            throw new RoleAlreadyExistsException("User already has this Role. Role: " + role.toString());
        }
        user.getRoles().add(role);
        String r = role.toString();
        try {
            UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(id, r);
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_ADD_ROLE, updateAuthUserRoleRequest);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send add role message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        return this.userMapper.toGetUserResponse(user);
    }

    @Override
    @Transactional
    public GetUserResponse removeRole(Long id, Role role) {
        User user = this.userRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User doesn't exist with id " + id);
                });
        if (user.getRoles().size() <= 1) {
            throw new SingleRoleRemovalException("Cannot remove role. User must have at least one role");
        }
        if (!user.getRoles().contains(role)) {
            throw new RoleNotFoundException("The user does not own this role! Role: " + role);
        }
        user.getRoles().remove(role);
        String r = role.toString();
        try {
            UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(id, r);
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_REMOVE_ROLE, updateAuthUserRoleRequest);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send remove role message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        return this.userMapper.toGetUserResponse(user);
    }
}

