package com.lab.backend.auth.service.concretes;

import com.lab.backend.auth.dto.requests.*;
import com.lab.backend.auth.entity.Role;
import com.lab.backend.auth.entity.Token;
import com.lab.backend.auth.entity.User;
import com.lab.backend.auth.repository.RoleRepository;
import com.lab.backend.auth.repository.TokenRepository;
import com.lab.backend.auth.repository.UserRepository;
import com.lab.backend.auth.service.abstracts.JwtService;
import com.lab.backend.auth.service.abstracts.UserService;
import com.lab.backend.auth.utilities.exceptions.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final MailService mailService;
    private JedisPool jedisPool;
    private final WebClient.Builder webClientBuilder;

    @PostConstruct
    public void init() {
        this.jedisPool = new JedisPool(redisHost, Integer.parseInt(redisPort));
    }

    @PreDestroy
    public void shutDown() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    @Override
    public List<String> login(AuthRequest authRequest) throws RedisOperationException {
        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Authentication failed! Username or password is incorrect");
        }
        User user = this.userRepository.findByUsernameAndDeletedIsFalse(authRequest.getUsername())
                .orElseThrow(() -> {
                    return new UserNotFoundException("User not found with username: " + authRequest.getUsername());
                });

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email address not verified for user: " + authRequest.getUsername());
        }

        String accessToken = this.jwtService.generateAccessToken(authRequest.getUsername(), getRolesAsString(user.getRoles()));
        String refreshToken = this.jwtService.generateRefreshToken(authRequest.getUsername());

        revokeAllTokensByUser(user.getId());
        saveUserToken(user, accessToken);

        return Arrays.asList(accessToken, refreshToken);
    }

    @Override
    public List<String> refreshToken(HttpServletRequest request) throws UsernameExtractionException, InvalidTokenException, RedisOperationException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String username = this.jwtService.extractUsername(refreshToken);

            if (username != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (userDetails != null && jwtService.isTokenValid(refreshToken, userDetails)) {
                    User user = this.userRepository.findByUsernameAndDeletedIsFalse(username)
                            .orElseThrow(() -> {
                                return new UserNotFoundException("User not found with username: " + username);
                            });

                    if (!user.isEmailVerified()) {
                        throw new EmailNotVerifiedException("Email address not verified for user: " + username);
                    }

                    String accessToken = this.jwtService.generateAccessToken(username, getRolesAsString(user.getRoles()));
                    revokeAllTokensByUser(user.getId());
                    saveUserToken(user, accessToken);
                    return Arrays.asList(accessToken, refreshToken);
                } else {
                    throw new UserNotFoundException("User not found");
                }
            } else {
                throw new UsernameExtractionException("Username extraction failed");
            }
        } else {
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    private void saveUserToken(User user, String jwt) throws RedisOperationException {
        Token token = Token.builder()
                .token(jwt)
                .user(user)
                .loggedOut(false)
                .build();
        this.tokenRepository.save(token);

        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.set("token:" + token.getId() + ":is_logged_out", "false");
            jedis.set(jwt, String.valueOf(token.getId()));
        } catch (JedisException e) {
            throw new RedisOperationException("Failed to set token status in Redis ", e);
        }
    }

    private void revokeAllTokensByUser(long id) throws RedisOperationException {
        List<Token> validTokens = this.tokenRepository.findAllValidTokensByUser(id);
        if (validTokens.isEmpty()) {
            return;
        }

        try (Jedis jedis = this.jedisPool.getResource()) {
            for (Token token : validTokens) {
                token.setLoggedOut(true);
                jedis.set("token:" + token.getId() + ":is_logged_out", "true");
            }
        } catch (JedisException e) {
            throw new RedisOperationException("Failed to set token status in Redis ", e);
        }
        this.tokenRepository.saveAll(validTokens);
    }

    @Override
    public void logout(HttpServletRequest request) throws InvalidTokenException, RedisOperationException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid token");
        }
        String jwt = authHeader.substring(7);
        Token storedToken = this.tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken == null) {
            throw new TokenNotFoundException("Token not found");
        }
        storedToken.setLoggedOut(true);
        this.tokenRepository.save(storedToken);
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.set("token:" + storedToken.getId() + ":is_logged_out", "true");
        } catch (JedisException e) {
            throw new RedisOperationException("Failed to log out token in Redis ", e);
        }
    }

    @Override
    public String changePassword(HttpServletRequest request, PasswordRequest passwordRequest) throws RedisOperationException, InvalidTokenException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid token");
        }
        String jwt = authHeader.substring(7);
        String username = this.jwtService.extractUsername(jwt);
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (this.passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword())) {
                user.setPassword(this.passwordEncoder.encode(passwordRequest.getNewPassword()));
                this.userRepository.save(user);
                revokeAllTokensByUser(user.getId());
                return "Password changed successfully.";
            } else {
                throw new IncorrectPasswordException("Password is incorrect");
            }
        } else {
            throw new UserNotFoundException("User not found! User may not be logged in");
        }
    }

    @Override
    public void initiatePasswordReset(String email) {
        String username;
        try {
            username = this.webClientBuilder.build().get()
                    .uri("http://user-management-service/users/email", uriBuilder ->
                            uriBuilder.queryParam("email", email).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new UserNotFoundException("User not found in user management service with email: " + email);
        } catch (Exception e) {
            throw new UnexpectedException("Error occurred while calling user management service: " + e);
        }
        if (username != null) {
            Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(username);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String resetToken = generateResetToken();
                user.setResetToken(resetToken);
                user.setResetTokenExpiration(calculateResetTokenExpiration());
                this.userRepository.save(user);
                sendPasswordResetEmail(user.getUsername(), email, resetToken);
            } else {
                throw new UserNotFoundException("User not found with email: " + email);
            }
        } else {
            throw new UserNotFoundException("User not found in user management service with email: " + email);
        }
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    private Date calculateResetTokenExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        return calendar.getTime();
    }

    private void sendPasswordResetEmail(String username, String userMail, String resetToken) {
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + resetToken;
        String message = String.format("Hello %s,\n\nYou requested a password reset. Please use the following link to reset your password:\n%s\n\nIf you did not request this, please ignore this email.\n\nÖMER ASAF BALIKÇI", username, resetUrl);

        this.mailService.sendEmail(userMail, "Password Reset Request", message);
    }

    @Override
    public String handlePasswordReset(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        Optional<User> optionalUser = this.userRepository.findByResetTokenAndDeletedIsFalse(token);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getResetTokenExpiration().before(new Date())) {
                return "Token has expired";
            }
            user.setPassword(this.passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiration(null);
            this.userRepository.save(user);
            return "Password reset successfully.";
        } else {
            return "Invalid token";
        }
    }

    private List<String> getRolesAsString(List<Role> roles) {
        try {
            return roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        } finally {
            log.trace("Exiting getRolesAsString method in AuthServiceImpl");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.create}")
    public void createUser(CreateAuthUserRequest createAuthUserRequest) throws UserAlreadyExistsException, NoRolesException, InvalidEmailFormatException, EmailSendingFailedException {
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(createAuthUserRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + createAuthUserRequest.getUsername() + "' is already taken");
        }
        if (createAuthUserRequest.getRoles().isEmpty()) {
            throw new NoRolesException("No role found for registration!");
        }
        User user = User.builder()
                .username(createAuthUserRequest.getUsername())
                .password(this.passwordEncoder.encode(createAuthUserRequest.getPassword()))
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();
        List<Role> roles = new ArrayList<>();
        for (String role : createAuthUserRequest.getRoles()) {
            Optional<Role> roleOptional = this.roleRepository.findByName(role);
            roleOptional.ifPresent(roles::add);
        }
        if (roles.isEmpty()) {
            throw new NoRolesException("No valid roles found");
        }
        user.setRoles(roles);
        this.userRepository.save(user);

        String verificationLink = "http://localhost:8080/auth/verify-email?token=" + user.getEmailVerificationToken();
        String emailSubject = "Please Verify Your Email";
        String emailBody = "Please click the following link to verify your email address: " + verificationLink;
        this.mailService.sendEmail(createAuthUserRequest.getEmail(), emailSubject, emailBody);
    }

    @Override
    public void verifyEmail(String token) {
        User user = this.userRepository.findByEmailVerificationTokenAndDeletedIsFalse(token)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User with the given verification token not found");
                });
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        this.userRepository.save(user);
    }

    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUser(UpdateAuthUserRequest updateAuthUserRequest) throws UserAlreadyExistsException, UserNotFoundException {
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(updateAuthUserRequest.getNewUsername())) {
            throw new UserAlreadyExistsException("Username is already taken! Username: " + updateAuthUserRequest.getNewUsername());
        }
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(updateAuthUserRequest.getOldUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(updateAuthUserRequest.getNewUsername());
            this.userRepository.save(user);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(String username) throws RedisOperationException, UserNotFoundException {
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setDeleted(true);
            this.userRepository.save(user);
            revokeAllTokensByUser(user.getId());
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(String username) throws RedisOperationException, UserNotFoundException {
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsTrue(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setDeleted(false);
            this.userRepository.save(user);
            revokeAllTokensByUser(user.getId());
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.addRole}")
    public void addRole(UpdateAuthUserRoleRequest updateAuthUserRoleRequest) throws RoleNotFoundException, UserNotFoundException {
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(updateAuthUserRoleRequest.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Role> optionalRole = this.roleRepository.findByName(updateAuthUserRoleRequest.getRole());
            if (optionalRole.isPresent()) {
                user.getRoles().add(optionalRole.get());
                this.userRepository.save(user);
            } else {
                throw new RoleNotFoundException("Role not found");
            }
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.removeRole}")
    public void removeRole(UpdateAuthUserRoleRequest updateAuthUserRoleRequest) throws RoleNotFoundException, UserNotFoundException {
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(updateAuthUserRoleRequest.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Role> optionalRole = this.roleRepository.findByName(updateAuthUserRoleRequest.getRole());
            if (optionalRole.isPresent()) {
                user.getRoles().remove(optionalRole.get());
                this.userRepository.save(user);
            } else {
                throw new RoleNotFoundException("Role not found");
            }
        } else {
            throw new UserNotFoundException("User not found");
        }
    }
}
