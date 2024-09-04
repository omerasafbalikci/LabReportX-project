package com.lab.backend.usermanagement.service.concretes;

import com.lab.backend.usermanagement.dao.RoleRepository;
import com.lab.backend.usermanagement.dao.UserRepository;
import com.lab.backend.usermanagement.dao.UserSpecification;
import com.lab.backend.usermanagement.dto.requests.AuthUserRequest;
import com.lab.backend.usermanagement.dto.requests.CreateUserRequest;
import com.lab.backend.usermanagement.dto.requests.UpdateUserRequest;
import com.lab.backend.usermanagement.dto.responses.GetUserResponse;
import com.lab.backend.usermanagement.dto.responses.PagedResponse;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.service.abstracts.UserService;
import com.lab.backend.usermanagement.utilities.exceptions.RabbitMQException;
import com.lab.backend.usermanagement.utilities.exceptions.RoleNotFoundException;
import com.lab.backend.usermanagement.utilities.exceptions.UserAlreadyExistsException;
import com.lab.backend.usermanagement.utilities.exceptions.UserNotFoundException;
import com.lab.backend.usermanagement.utilities.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    @Value("#{'${default-roles}'.split(', ')}")
    private final List<String> DEFAULT_ROLES;

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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public GetUserResponse getUserById(Long id) {
        User user = this.userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("User not found with id: {}", id);
            return new UserNotFoundException("User not found with id: " + id);
        });
        GetUserResponse response = this.userMapper.toGetUserResponse(user);
        log.debug("Successfully retrieved user with id: {}", id);
        log.trace("Exiting getUserById method in UserServiceImpl with id: {}", id);
        return response;
    }

    @Override
    public PagedResponse<GetUserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                       String lastName, String username, String email, String role, String gender,
                                                                       Boolean deleted) {
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        UserSpecification specification = new UserSpecification(firstName, lastName, username, email, role, gender, deleted);
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
    public GetUserResponse createUser(CreateUserRequest createUserRequest) {
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(createUserRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + createUserRequest.getUsername() + "' is already taken");
        }
        if (this.userRepository.existsByEmailAndDeletedIsFalse(createUserRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email '" + createUserRequest.getEmail() + "' is already taken");
        }
        User user = this.userMapper.toUser(createUserRequest);

        try {
            AuthUserRequest authUserRequest = new AuthUserRequest(createUserRequest.getUsername(), createUserRequest.getPassword(), createUserRequest.getRoles());
            this.rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREATE, authUserRequest);
        } catch (Exception exception) {
            throw new RabbitMQException("Failed to send create message to RabbitMQ", exception);
        }
        this.userRepository.save(user);
        return this.userMapper.toGetUserResponse(user);
    }

    @Override
    public GetUserResponse updateUser(UpdateUserRequest updateUserRequest) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }

    @Override
    public GetUserResponse restoreUser(Long id) {
        return null;
    }

    private void validateRoles(List<String> roles) {
        for (String role : roles) {
            if (!DEFAULT_ROLES.contains(role)) {
                throw new RoleNotFoundException("Role not found: " + role);
            }
        }
    }
}
