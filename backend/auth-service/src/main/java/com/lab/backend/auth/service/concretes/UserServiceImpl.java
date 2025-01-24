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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service implementation for managing users.
 *
 * @author Ömer Asaf BALIKÇI
 */

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
    private static final String BEARER = "Bearer ";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String IS_LOGGED_OUT = ":is_logged_out";
    private static final String TOKEN = "token:";
    private static final String INVALID_TOKEN = "Invalid token";

    @PostConstruct
    public void init() {
        log.trace("Initializing JedisPool with Redis Host: {} and Port: {}", redisHost, redisPort);
        this.jedisPool = new JedisPool(redisHost, Integer.parseInt(redisPort));
        log.trace("Exiting init method in UserServiceImpl");
    }

    @PreDestroy
    public void shutDown() {
        if (jedisPool != null) {
            log.trace("Shutting down JedisPool");
            jedisPool.close();
        }
    }

    /**
     * Logs in a user and generates JWT tokens if authentication is successful.
     *
     * @param authRequest the authentication request containing username and password.
     * @return a list containing the access token and refresh token.
     * @throws RedisOperationException if there is an issue interacting with Redis.
     */
    @Override
    public List<String> login(AuthRequest authRequest) throws RedisOperationException {
        log.trace("Entering login method in UserServiceImpl");
        log.debug("Attempting to authenticate user: {}", authRequest.getUsername());
        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", authRequest.getUsername());
            throw new AuthenticationFailedException("Authentication failed! Username or password is incorrect");
        }
        User user = this.userRepository.findByUsernameAndDeletedIsFalse(authRequest.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", authRequest.getUsername());
                    return new UserNotFoundException("User not found with username: " + authRequest.getUsername());
                });
        if (!user.isEmailVerified()) {
            log.error("Email not verified for user: {}", authRequest.getUsername());
            throw new EmailNotVerifiedException("Email address not verified for user: " + authRequest.getUsername());
        }
        log.info("User authenticated successfully: {}", authRequest.getUsername());
        String accessToken = this.jwtService.generateAccessToken(authRequest.getUsername(), getRolesAsString(user.getRoles()));
        String refreshToken = this.jwtService.generateRefreshToken(authRequest.getUsername());

        revokeAllTokensByUser(user.getId());
        saveUserToken(user, accessToken);
        log.trace("Exiting login method in UserServiceImpl");
        return Arrays.asList(accessToken, refreshToken);
    }

    /**
     * Refreshes the access token if the refresh token is valid.
     *
     * @param request the HTTP request containing the refresh token.
     * @return a list containing the new access token and refresh token.
     * @throws UsernameExtractionException if the username cannot be extracted from the token.
     * @throws InvalidTokenException       if the token is invalid.
     * @throws RedisOperationException     if there is an issue interacting with Redis.
     */
    @Override
    public List<String> refreshToken(HttpServletRequest request) throws UsernameExtractionException, InvalidTokenException, RedisOperationException {
        log.trace("Entering refreshToken method in UserServiceImpl");
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("Attempting to refresh token");
        if (authHeader != null && authHeader.startsWith(BEARER)) {
            String refreshToken = authHeader.substring(7);
            String username = this.jwtService.extractUsername(refreshToken);
            log.info("Extracted username from refresh token: {}", username);
            if (username != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (userDetails != null && jwtService.isTokenValid(refreshToken, userDetails)) {
                    User user = this.userRepository.findByUsernameAndDeletedIsFalse(username)
                            .orElseThrow(() -> {
                                log.error("User not found during token refresh: {}", username);
                                return new UserNotFoundException("User not found with username: " + username);
                            });
                    if (!user.isEmailVerified()) {
                        log.error("Email not verified for user {} so refresh token cannot be done.", username);
                        throw new EmailNotVerifiedException("Email address not verified for user: " + username);
                    }
                    log.info("Generating new tokens for user: {}", username);
                    String accessToken = this.jwtService.generateAccessToken(username, getRolesAsString(user.getRoles()));
                    revokeAllTokensByUser(user.getId());
                    saveUserToken(user, accessToken);
                    log.trace("Exiting refreshToken method in UserServiceImpl");
                    return Arrays.asList(accessToken, refreshToken);
                } else {
                    log.error("User not found : {}", username);
                    throw new UserNotFoundException(USER_NOT_FOUND);
                }
            } else {
                log.error("Username extraction failed from token");
                throw new UsernameExtractionException("Username extraction failed");
            }
        } else {
            log.error("Invalid token format");
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    /**
     * Saves the user's JWT token in the database and Redis.
     *
     * @param user the user associated with the token.
     * @param jwt  the JWT token to be saved.
     * @throws RedisOperationException if there is an issue interacting with Redis.
     */
    public void saveUserToken(User user, String jwt) throws RedisOperationException {
        log.trace("Entering saveUserToken method in UserServiceImpl");
        log.debug("Saving token for user: {}", user.getUsername());
        Token token = Token.builder()
                .token(jwt)
                .user(user)
                .loggedOut(false)
                .build();
        this.tokenRepository.save(token);

        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.set(TOKEN + token.getId() + IS_LOGGED_OUT, "false");
            jedis.set(jwt, String.valueOf(token.getId()));
            log.info("Token saved in Redis for user: {}", user.getUsername());
        } catch (JedisException e) {
            log.error("Failed to save token in Redis for user: {}", user.getUsername());
            throw new RedisOperationException("Failed to set token status in Redis ", e);
        }
        log.trace("Exiting saveUserToken method in UserServiceImpl");
    }

    /**
     * Revokes all valid tokens associated with the user.
     *
     * @param id the ID of the user whose tokens will be revoked.
     * @throws RedisOperationException if there is an issue interacting with Redis.
     */
    private void revokeAllTokensByUser(long id) throws RedisOperationException {
        log.trace("Entering revokeAllTokensByUser method in UserServiceImpl");
        log.debug("Revoking all tokens for user ID: {}", id);
        List<Token> validTokens = this.tokenRepository.findAllValidTokensByUser(id);
        if (validTokens.isEmpty()) {
            log.info("No valid tokens found for user ID: {}", id);
            return;
        }

        try (Jedis jedis = this.jedisPool.getResource()) {
            for (Token token : validTokens) {
                token.setLoggedOut(true);
                jedis.set(TOKEN + token.getId() + IS_LOGGED_OUT, "true");
                log.info("Token revoked for user ID: {}", id);
            }
        } catch (JedisException e) {
            log.error("Failed to revoke tokens in Redis for user ID: {}", id);
            throw new RedisOperationException("Failed to set token status in Redis ", e);
        }
        this.tokenRepository.saveAll(validTokens);
        log.trace("Exiting revokeAllTokensByUser method in UserServiceImpl");
    }

    /**
     * Logs out the user by invalidating the JWT token and updating the Redis cache.
     *
     * @param request the HTTP request containing the token
     * @throws InvalidTokenException   if the token is invalid or not provided
     * @throws RedisOperationException if there is an error while interacting with Redis
     */
    @Override
    public void logout(HttpServletRequest request) throws InvalidTokenException, RedisOperationException {
        log.trace("Entering logout method in UserServiceImpl");
        log.debug("Logout initiated");
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            log.error(INVALID_TOKEN);
            throw new InvalidTokenException(INVALID_TOKEN);
        }
        String jwt = authHeader.substring(7);
        Token storedToken = this.tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken == null) {
            log.error("Token not found in repository");
            throw new TokenNotFoundException("Token not found");
        }
        storedToken.setLoggedOut(true);
        this.tokenRepository.save(storedToken);
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.set(TOKEN + storedToken.getId() + IS_LOGGED_OUT, "true");
            log.info("Token successfully logged out and saved in Redis");
        } catch (JedisException e) {
            log.error("Failed to log out token in Redis", e);
            throw new RedisOperationException("Failed to log out token in Redis ", e);
        }
        log.trace("Exiting logout method in UserServiceImpl");
    }

    /**
     * Changes the user's password if the old password matches and invalidates previous tokens.
     *
     * @param request         the HTTP request containing the token
     * @param passwordRequest the old and new passwords
     * @return a success message if the password is successfully changed
     * @throws RedisOperationException if there is an error interacting with Redis
     * @throws InvalidTokenException   if the token is invalid
     */
    @Override
    public String changePassword(HttpServletRequest request, PasswordRequest passwordRequest) throws RedisOperationException, InvalidTokenException {
        log.trace("Entering changePassword method in UserServiceImpl");
        log.debug("Password change request initiated");
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            log.error("Invalid token format to change password");
            throw new InvalidTokenException(INVALID_TOKEN);
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
                log.info("Password changed successfully for user: {}", username);
                log.trace("Exiting changePassword method in UserServiceImpl");
                return "Password changed successfully.";
            } else {
                log.error("Old password is incorrect for user: {}", username);
                throw new IncorrectPasswordException("Password is incorrect");
            }
        } else {
            log.error("User not found for username: {}", username);
            throw new UserNotFoundException("User not found! User may not be logged in");
        }
    }

    /**
     * Initiates a password reset process for the given email by generating a reset token and sending an email.
     *
     * @param email the email of the user requesting password reset
     */
    @Override
    public void initiatePasswordReset(String email) {
        log.trace("Entering initiatePasswordReset method in UserServiceImpl");
        String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        if (!pattern.matcher(email).matches()) {
            log.error("Invalid email format: {}", email);
            throw new InvalidEmailFormatException("The provided email has an invalid format: " + email);
        }
        String username;
        try {
            username = this.webClientBuilder.build().get()
                    .uri("http://user-management-service/users/email", uriBuilder ->
                            uriBuilder.queryParam("email", email).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            log.error("User not found in user management service for email: {}", email);
            throw new UserNotFoundException("User not found in user management service with email: " + email);
        } catch (Exception e) {
            log.error("Error occurred while calling user management service", e);
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
                log.info("Password reset email sent to user: {}", username);
            } else {
                log.error("User not found with email: {}", email);
                throw new UserNotFoundException("User not found with email: " + email);
            }
        } else {
            log.error("User not found in user management service for email: {}", email);
            throw new UserNotFoundException("User not found in user management service with email: " + email);
        }
        log.trace("Exiting initiatePasswordReset method in UserServiceImpl");
    }

    /**
     * Generates a unique reset token for password reset requests.
     *
     * @return a randomly generated UUID as a string representing the reset token
     */
    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Calculates the expiration time for the password reset token.
     * The token will be valid for 1 hour from the time of generation.
     *
     * @return a Date object representing the expiration time of the reset token
     */
    public Date calculateResetTokenExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        return calendar.getTime();
    }

    /**
     * Sends a password reset email to the user with a reset link.
     *
     * @param username   the username of the user requesting the password reset
     * @param userMail   the email address of the user to which the reset email will be sent
     * @param resetToken the generated reset token to be included in the email link
     */
    public void sendPasswordResetEmail(String username, String userMail, String resetToken) {
        log.trace("Entering sendPasswordResetEmail method in UserServiceImpl");
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + resetToken;
        String message = String.format("Hello %s,\n\nYou requested a password reset. Please use the following link to reset your password:\n%s\n\nIf you did not request this, please ignore this email.\n\nÖMER ASAF BALIKÇI", username, resetUrl);

        this.mailService.sendEmail(userMail, "Password Reset Request", message);
        log.trace("Exiting sendPasswordResetEmail method in UserServiceImpl");
    }

    /**
     * Handles the password reset process using the provided token and new password.
     *
     * @param token       the reset token
     * @param newPassword the new password
     * @return a message indicating success or failure
     */
    @Override
    public String handlePasswordReset(String token, String newPassword) {
        log.trace("Entering handlePasswordReset method in UserServiceImpl");
        log.debug("Handling password reset for token: {}", token);
        if (newPassword == null || newPassword.length() < 8) {
            log.error("New password does not meet the minimum length requirement");
            throw new InvalidPasswordException("Password must be at least 8 characters long.");
        }
        Optional<User> optionalUser = this.userRepository.findByResetTokenAndDeletedIsFalse(token);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getResetTokenExpiration().before(new Date())) {
                log.error("Reset token has expired for user: {}", user.getUsername());
                throw new InvalidTokenException("Token has expired");
            }
            user.setPassword(this.passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiration(null);
            this.userRepository.save(user);
            log.info("Password reset successfully for user: {}", user.getUsername());
            log.trace("Exiting handlePasswordReset method in UserServiceImpl");
            return "Password reset successfully.";
        } else {
            log.error("Invalid reset token provided");
            throw new InvalidTokenException(INVALID_TOKEN);
        }
    }

    /**
     * Converts a list of Role objects into a list of role names as strings.
     *
     * @param roles the list of Role objects to be converted
     * @return a list of role names as strings
     */
    private List<String> getRolesAsString(List<Role> roles) {
        log.trace("Entering getRolesAsString method in UserServiceImpl");
        try {
            return roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        } finally {
            log.trace("Exiting getRolesAsString method in UserServiceImpl");
        }
    }

    /**
     * Creates a new user based on the provided request and sends a verification email.
     *
     * @param createAuthUserRequest the request containing user information
     * @throws UserAlreadyExistsException  if the username already exists
     * @throws NoRolesException            if no roles are provided
     * @throws InvalidEmailFormatException if the email format is invalid
     * @throws EmailSendingFailedException if the email could not be sent
     */
    @RabbitListener(queues = "${rabbitmq.queue.create}")
    public void createUser(CreateAuthUserRequest createAuthUserRequest) throws UserAlreadyExistsException, NoRolesException, InvalidEmailFormatException, EmailSendingFailedException {
        log.trace("Entering createUser method in UserServiceImpl");
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(createAuthUserRequest.getUsername())) {
            log.error("Username '{}' is already taken", createAuthUserRequest.getUsername());
            throw new UserAlreadyExistsException("Username '" + createAuthUserRequest.getUsername() + "' is already taken");
        }
        if (createAuthUserRequest.getRoles().isEmpty()) {
            log.error("No role found for registration!");
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
            log.error("No valid roles found");
            throw new NoRolesException("No valid roles found");
        }
        user.setRoles(roles);
        this.userRepository.save(user);
        log.info("User '{}' created successfully", user.getUsername());
        String verificationLink = "http://localhost:8080/auth/verify-email?token=" + user.getEmailVerificationToken();
        String emailSubject = "Please Verify Your Email";
        String emailBody = "Please click the following link to verify your email address: " + verificationLink;
        this.mailService.sendEmail(createAuthUserRequest.getEmail(), emailSubject, emailBody);
        log.info("Verification email sent to '{}'", createAuthUserRequest.getEmail());
        log.trace("Exiting createUser method in UserServiceImpl");
    }

    /**
     * Verifies the user's email using the provided token.
     *
     * @param token the email verification token
     * @throws UserNotFoundException if no user is found with the given token
     */
    @Override
    public void verifyEmail(String token) {
        log.trace("Entering verifyEmail method in UserServiceImpl");
        User user = this.userRepository.findByEmailVerificationTokenAndDeletedIsFalse(token)
                .orElseThrow(() -> {
                    log.error("User with the given verification token not found");
                    return new UserNotFoundException("User with the given verification token not found");
                });
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        this.userRepository.save(user);
        log.info("Email verified for user: {}", user.getUsername());
        log.trace("Exiting verifyEmail method in UserServiceImpl");
    }

    /**
     * Updates a user's username based on the provided request.
     *
     * @param updateAuthUserRequest the request containing user information
     * @throws UserAlreadyExistsException if the new username already exists
     * @throws UserNotFoundException      if the user does not exist
     */
    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUser(UpdateAuthUserRequest updateAuthUserRequest) throws UserAlreadyExistsException, UserNotFoundException {
        log.trace("Entering updateUser method in UserServiceImpl");
        if (this.userRepository.existsByUsernameAndDeletedIsFalse(updateAuthUserRequest.getNewUsername())) {
            log.error("Username is already taken: {}", updateAuthUserRequest.getNewUsername());
            throw new UserAlreadyExistsException("Username is already taken! Username: " + updateAuthUserRequest.getNewUsername());
        }
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(updateAuthUserRequest.getOldUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(updateAuthUserRequest.getNewUsername());
            this.userRepository.save(user);
            log.info("User '{}' updated successfully", user.getUsername());
        } else {
            log.error(USER_NOT_FOUND);
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        log.trace("Exiting updateUser method in UserServiceImpl");
    }

    /**
     * Deletes a user by marking them as deleted.
     *
     * @param username the username of the user to be deleted
     * @throws RedisOperationException if there is an error with Redis operations
     * @throws UserNotFoundException   if the user does not exist
     */
    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(String username) throws RedisOperationException, UserNotFoundException {
        log.trace("Entering deleteUser method in UserServiceImpl");
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setDeleted(true);
            this.userRepository.save(user);
            revokeAllTokensByUser(user.getId());
            log.info("User '{}' marked as deleted", username);
        } else {
            log.error("User not found: {}", username);
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        log.trace("Exiting deleteUser method in UserServiceImpl");
    }

    /**
     * Restores a previously deleted user.
     *
     * @param username the username of the user to be restored
     * @throws RedisOperationException if there is an error with Redis operations
     * @throws UserNotFoundException   if the user does not exist
     */
    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(String username) throws RedisOperationException, UserNotFoundException {
        log.trace("Entering restoreUser method in UserServiceImpl");
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsTrue(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setDeleted(false);
            this.userRepository.save(user);
            revokeAllTokensByUser(user.getId());
            log.info("User '{}' restored successfully", username);
        } else {
            log.error("User not found to restore: {}", username);
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        log.trace("Exiting restoreUser method in UserServiceImpl");
    }

    /**
     * Adds a role to a user based on the provided request.
     *
     * @param updateAuthUserRoleRequest the request containing user and role information
     * @throws RoleNotFoundException if the specified role does not exist
     * @throws UserNotFoundException if the user does not exist
     */
    @RabbitListener(queues = "${rabbitmq.queue.addRole}")
    public void addRole(UpdateAuthUserRoleRequest updateAuthUserRoleRequest) throws RoleNotFoundException, UserNotFoundException {
        log.trace("Entering addRole method in UserServiceImpl");
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(updateAuthUserRoleRequest.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Role> optionalRole = this.roleRepository.findByName(updateAuthUserRoleRequest.getRole());
            if (optionalRole.isPresent()) {
                user.getRoles().add(optionalRole.get());
                this.userRepository.save(user);
                log.info("Role '{}' added to user '{}'", updateAuthUserRoleRequest.getRole(), updateAuthUserRoleRequest.getUsername());
            } else {
                log.error("Role not found: {}", updateAuthUserRoleRequest.getRole());
                throw new RoleNotFoundException("Role not found");
            }
        } else {
            log.error("No user found to add role: {}", updateAuthUserRoleRequest.getUsername());
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        log.trace("Exiting addRole method in UserServiceImpl");
    }

    /**
     * Removes a role from a user based on the provided request.
     *
     * @param updateAuthUserRoleRequest the request containing user and role information
     * @throws RoleNotFoundException if the specified role does not exist
     * @throws UserNotFoundException if the user does not exist
     */
    @RabbitListener(queues = "${rabbitmq.queue.removeRole}")
    public void removeRole(UpdateAuthUserRoleRequest updateAuthUserRoleRequest) throws RoleNotFoundException, UserNotFoundException {
        log.trace("Entering removeRole method in UserServiceImpl");
        Optional<User> optionalUser = this.userRepository.findByUsernameAndDeletedIsFalse(updateAuthUserRoleRequest.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Role> optionalRole = this.roleRepository.findByName(updateAuthUserRoleRequest.getRole());
            if (optionalRole.isPresent()) {
                user.getRoles().remove(optionalRole.get());
                this.userRepository.save(user);
                log.info("Role '{}' removed from user '{}'", updateAuthUserRoleRequest.getRole(), updateAuthUserRoleRequest.getUsername());
            } else {
                log.error("Role not found to remove: {}", updateAuthUserRoleRequest.getRole());
                throw new RoleNotFoundException("Role not found");
            }
        } else {
            log.error("No user found to remove role: {}", updateAuthUserRoleRequest.getUsername());
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        log.trace("Exiting removeRole method in UserServiceImpl");
    }
}
