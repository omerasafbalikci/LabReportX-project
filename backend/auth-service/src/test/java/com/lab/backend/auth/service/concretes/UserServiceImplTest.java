package com.lab.backend.auth.service.concretes;

import com.lab.backend.auth.dto.requests.*;
import com.lab.backend.auth.entity.Role;
import com.lab.backend.auth.entity.Token;
import com.lab.backend.auth.entity.User;
import com.lab.backend.auth.repository.RoleRepository;
import com.lab.backend.auth.repository.TokenRepository;
import com.lab.backend.auth.repository.UserRepository;
import com.lab.backend.auth.service.abstracts.JwtService;
import com.lab.backend.auth.utilities.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private UserDetails userDetails;
    @Mock
    private MailService mailService;
    @Mock
    private JedisPool jedisPool;
    @InjectMocks
    private UserServiceImpl userService;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "redisHost", "localhost");
        ReflectionTestUtils.setField(userService, "redisPort", "6379");
        ReflectionTestUtils.setField(userService, "jedisPool", jedisPool);

        this.authRequest = new AuthRequest("testUser", "encodedPass");

        this.user = User.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPass")
                .emailVerified(true)
                .roles(List.of(new Role("ROLE_ADMIN")))
                .build();
    }

    @Test
    void givenValidRedisHostAndPort_whenInitMethodIsCalled_thenJedisPoolIsInitialized() throws RedisOperationException {
        // Arrange
        this.userService = spy(userService);
        lenient().doNothing().when(userService).saveUserToken(any(), anyString());

        // Act
        this.userService.init();
        JedisPool jedisPool = (JedisPool) ReflectionTestUtils.getField(userService, "jedisPool");

        // Assert
        assertNotNull(jedisPool, "Jedis instance should be initialized");
    }

    @Test
    void givenInvalidPort_whenInitMethodIsCalled_thenNumberFormatExceptionIsThrown() {
        // Arrange
        ReflectionTestUtils.setField(userService, "redisPort", "invalidPort");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> userService.init(), "Invalid port should throw NumberFormatException");
    }

    @Test
    void login_withValidCredentials_shouldReturnAccessTokenAndRefreshToken() throws RedisOperationException {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsernameAndDeletedIsFalse(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refreshToken");

        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.set(anyString(), anyString())).thenReturn("OK");

        // Act
        List<String> tokens = userService.login(authRequest);

        // Assert
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("accessToken", tokens.get(0));
        assertEquals("refreshToken", tokens.get(1));
    }

    @Test
    void login_withInvalidCredentials_shouldThrowAuthenticationFailedException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Authentication failed") {
                });

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> userService.login(authRequest));

        assertEquals("Authentication failed! Username or password is incorrect", exception.getMessage());
    }

    @Test
    void login_withNonExistentUser_shouldThrowUserNotFoundException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsernameAndDeletedIsFalse(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.login(authRequest));

        assertEquals("User not found with username: testUser", exception.getMessage());
    }

    @Test
    void login_withUnverifiedEmail_shouldThrowEmailNotVerifiedException() {
        // Arrange
        user.setEmailVerified(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsernameAndDeletedIsFalse(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        EmailNotVerifiedException exception = assertThrows(EmailNotVerifiedException.class,
                () -> userService.login(authRequest));

        assertEquals("Email address not verified for user: " + authRequest.getUsername(), exception.getMessage());
        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(jwtService, never()).generateRefreshToken(anyString());
    }

    @Test
    void refreshToken_withValidRefreshToken_shouldReturnNewAccessTokenAndRefreshToken() throws Exception {
        // Arrange
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(eq(refreshToken), any())).thenReturn(true);
        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(anyString(), anyList())).thenReturn("newAccessToken");

        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.set(anyString(), anyString())).thenReturn("OK");

        // Act
        List<String> tokens = userService.refreshToken(request);

        // Assert
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("newAccessToken", tokens.get(0));
        assertEquals(refreshToken, tokens.get(1));
    }

    @Test
    void refreshToken_withInvalidToken_shouldThrowInvalidTokenException() {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("NotBearer invalidRefreshToken");

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> userService.refreshToken(request));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshToken_withNonExistentUser_shouldThrowUserNotFoundException() {
        // Arrange
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_withInvalidUserDetails_shouldThrowUserNotFoundException() {
        // Arrange
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_withUsernameExtractionFailure_shouldThrowUsernameExtractionException() {
        // Arrange
        String refreshToken = "validRefreshToken";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(null);

        // Act & Assert
        UsernameExtractionException exception = assertThrows(UsernameExtractionException.class,
                () -> userService.refreshToken(request));

        assertEquals("Username extraction failed", exception.getMessage());
    }

    @Test
    void refreshToken_withUnverifiedEmail_shouldThrowEmailNotVerifiedException() throws InvalidTokenException {
        // Arrange
        user.setEmailVerified(false);
        String refreshToken = "validRefreshToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(user.getUsername());
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(userDetails);
        when(jwtService.isTokenValid(refreshToken, userDetails)).thenReturn(true);
        when(userRepository.findByUsernameAndDeletedIsFalse(user.getUsername())).thenReturn(Optional.of(user));

        // Act & Assert
        EmailNotVerifiedException exception = assertThrows(EmailNotVerifiedException.class,
                () -> userService.refreshToken(request));

        assertEquals("Email address not verified for user: " + user.getUsername(), exception.getMessage());
        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(jwtService, never()).generateRefreshToken(anyString());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_withValidToken_shouldSuccessfullyLogout() throws InvalidTokenException, RedisOperationException {
        // Arrange
        String jwt = "validJwtToken";
        String authHeader = "Bearer " + jwt;
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);

        Token token = new Token();
        token.setId(1L);
        token.setToken(jwt);
        token.setLoggedOut(false);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.of(token));

        Jedis jedisMock = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedisMock);

        // Act
        userService.logout(request);

        // Assert
        verify(tokenRepository).save(token);
        assertTrue(token.isLoggedOut());
        verify(jedisMock).set("token:" + token.getId() + ":is_logged_out", "true");
    }

    @Test
    void logout_withInvalidAuthHeader_shouldThrowInvalidTokenException() {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> userService.logout(request));

        assertEquals("Invalid token", exception.getMessage());
        verify(tokenRepository, never()).findByToken(anyString());
    }

    @Test
    void logout_withTokenNotFound_shouldThrowTokenNotFoundException() {
        // Arrange
        String jwt = "nonExistentJwtToken";
        String authHeader = "Bearer " + jwt;
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.empty());

        // Act & Assert
        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class,
                () -> userService.logout(request));

        assertEquals("Token not found", exception.getMessage());
        verify(jedisPool, never()).getResource();
    }

    @Test
    void logout_withRedisFailure_shouldThrowRedisOperationException() throws InvalidTokenException {
        // Arrange
        String jwt = "validJwtToken";
        String authHeader = "Bearer " + jwt;
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);

        Token token = new Token();
        token.setId(1L);
        token.setToken(jwt);
        token.setLoggedOut(false);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.of(token));

        Jedis jedisMock = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedisMock);
        doThrow(new JedisException("Redis error")).when(jedisMock).set(anyString(), anyString());

        // Act & Assert
        RedisOperationException exception = assertThrows(RedisOperationException.class,
                () -> userService.logout(request));

        assertEquals("Failed to log out token in Redis ", exception.getMessage());
        verify(tokenRepository).save(token);
    }

    @Test
    void changePassword_success() throws Exception {
        // Arrange
        String token = "Bearer someValidToken";
        String newPassword = "newPassword";
        String oldPassword = "oldPassword";
        PasswordRequest passwordRequest = new PasswordRequest(oldPassword, newPassword);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
        when(jwtService.extractUsername("someValidToken")).thenReturn("testUser");
        when(userRepository.findByUsernameAndDeletedIsFalse("testUser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        String result = userService.changePassword(request, passwordRequest);

        // Assert
        assertEquals("Password changed successfully.", result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_invalidTokenException() {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.changePassword(request, new PasswordRequest("oldPassword", "newPassword")));
    }

    @Test
    void changePassword_incorrectPassword() {
        // Arrange
        String token = "Bearer someValidToken";
        PasswordRequest passwordRequest = new PasswordRequest("oldPassword", "newPassword");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
        when(jwtService.extractUsername("someValidToken")).thenReturn("testUser");
        when(userRepository.findByUsernameAndDeletedIsFalse("testUser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(IncorrectPasswordException.class, () -> userService.changePassword(request, passwordRequest));
    }

    @Test
    void changePassword_userNotFound() {
        // Arrange
        String token = "Bearer someValidToken";
        PasswordRequest passwordRequest = new PasswordRequest("oldPassword", "newPassword");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
        when(jwtService.extractUsername("someValidToken")).thenReturn("testUser");
        when(userRepository.findByUsernameAndDeletedIsFalse("testUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.changePassword(request, passwordRequest));
    }

    @Test
    void handlePasswordReset_shouldThrowException_whenPasswordIsInvalid() {
        // Arrange
        String token = "validToken";
        String shortPassword = "short";

        // Act & Assert
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.handlePasswordReset(token, shortPassword));

        assertEquals("Password must be at least 8 characters long.", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void handlePasswordReset_shouldThrowException_whenTokenIsInvalid() {
        // Arrange
        String token = "invalidToken";
        String newPassword = "newPassword123";
        when(userRepository.findByResetTokenAndDeletedIsFalse(token)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> userService.handlePasswordReset(token, newPassword));

        assertEquals("Invalid token", exception.getMessage());
        verify(userRepository, times(1)).findByResetTokenAndDeletedIsFalse(token);
    }

    @Test
    void handlePasswordReset_shouldThrowException_whenTokenIsExpired() {
        // Arrange
        String token = "validToken";
        String newPassword = "newPassword123";
        User user = new User();
        user.setUsername("testUser");
        user.setResetToken(token);
        user.setResetTokenExpiration(new Date(System.currentTimeMillis() - 10000));

        when(userRepository.findByResetTokenAndDeletedIsFalse(token)).thenReturn(Optional.of(user));

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> userService.handlePasswordReset(token, newPassword));

        assertEquals("Token has expired", exception.getMessage());
        verify(userRepository, times(1)).findByResetTokenAndDeletedIsFalse(token);
    }

    @Test
    void handlePasswordReset_shouldResetPassword_whenTokenIsValid() {
        // Arrange
        String token = "validToken";
        String newPassword = "newPassword123";
        User user = new User();
        user.setUsername("testUser");
        user.setResetToken(token);
        user.setResetTokenExpiration(new Date(System.currentTimeMillis() + 10000));

        when(userRepository.findByResetTokenAndDeletedIsFalse(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        // Act
        String result = userService.handlePasswordReset(token, newPassword);

        // Assert
        assertEquals("Password reset successfully.", result);
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiration());
        assertEquals("encodedPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_shouldThrowException_whenUsernameExists() {
        // Arrange
        CreateAuthUserRequest request = new CreateAuthUserRequest("existingUser", "password123", null, Collections.singletonList("ADMIN"));

        when(userRepository.existsByUsernameAndDeletedIsFalse(request.getUsername())).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(request));

        assertEquals("Username 'existingUser' is already taken", exception.getMessage());
    }

    @Test
    void createUser_shouldThrowException_whenNoRolesProvided() {
        // Arrange
        CreateAuthUserRequest request = new CreateAuthUserRequest("existingUser", "password123", null, Collections.emptyList());

        // Act & Assert
        NoRolesException exception = assertThrows(NoRolesException.class,
                () -> userService.createUser(request));

        assertEquals("No role found for registration!", exception.getMessage());
    }

    @Test
    void createUser_shouldThrowException_whenNoValidRolesFound() {
        // Arrange
        CreateAuthUserRequest request = new CreateAuthUserRequest("existingUser", "password123", null, Collections.singletonList("INVALID_ROLE"));

        when(userRepository.existsByUsernameAndDeletedIsFalse(request.getUsername())).thenReturn(false);
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        NoRolesException exception = assertThrows(NoRolesException.class,
                () -> userService.createUser(request));

        assertEquals("No valid roles found", exception.getMessage());
        verify(userRepository).existsByUsernameAndDeletedIsFalse(request.getUsername());
        verify(roleRepository).findByName("INVALID_ROLE");
    }

    @Test
    void createUser_shouldCreateUserAndSendEmail_whenAllIsValid() {
        // Arrange
        CreateAuthUserRequest request = new CreateAuthUserRequest("newUser", "password123", "newUser@example.com", Collections.singletonList("ADMIN"));

        when(userRepository.existsByUsernameAndDeletedIsFalse(request.getUsername())).thenReturn(false);
        Role role = new Role();
        role.setName("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        doNothing().when(mailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        userService.createUser(request);

        // Assert
        verify(userRepository).existsByUsernameAndDeletedIsFalse(request.getUsername());
        verify(roleRepository).findByName("ADMIN");
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(mailService).sendEmail(eq(request.getEmail()), anyString(), anyString());
    }

    @Test
    void verifyEmail_shouldVerifyEmail_whenTokenIsValid() {
        // Arrange
        String token = "validToken";
        User user = new User();
        user.setUsername("testUser");
        user.setEmailVerified(false);
        user.setEmailVerificationToken(token);

        when(userRepository.findByEmailVerificationTokenAndDeletedIsFalse(token)).thenReturn(Optional.of(user));

        // Act
        userService.verifyEmail(token);

        // Assert
        verify(userRepository).findByEmailVerificationTokenAndDeletedIsFalse(token);
        verify(userRepository).save(user);
        assert user.isEmailVerified();
        assert user.getEmailVerificationToken() == null;
    }

    @Test
    void verifyEmail_shouldThrowUserNotFoundException_whenTokenIsInvalid() {
        // Arrange
        String token = "invalidToken";
        when(userRepository.findByEmailVerificationTokenAndDeletedIsFalse(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.verifyEmail(token));

        verify(userRepository).findByEmailVerificationTokenAndDeletedIsFalse(token);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateUser_whenValidRequest() throws UserAlreadyExistsException, UserNotFoundException {
        // Arrange
        UpdateAuthUserRequest request = new UpdateAuthUserRequest("oldUsername", "newUsername");

        User user = new User();
        user.setUsername("oldUsername");

        when(userRepository.existsByUsernameAndDeletedIsFalse(request.getNewUsername())).thenReturn(false);
        when(userRepository.findByUsernameAndDeletedIsFalse(request.getOldUsername())).thenReturn(Optional.of(user));

        // Act
        userService.updateUser(request);

        // Assert
        verify(userRepository).existsByUsernameAndDeletedIsFalse(request.getNewUsername());
        verify(userRepository).findByUsernameAndDeletedIsFalse(request.getOldUsername());
        verify(userRepository).save(user);
        assert user.getUsername().equals("newUsername");
    }

    @Test
    void updateUser_shouldThrowUserAlreadyExistsException_whenNewUsernameTaken() {
        // Arrange
        UpdateAuthUserRequest request = new UpdateAuthUserRequest("oldUsername", "newUsername");

        when(userRepository.existsByUsernameAndDeletedIsFalse(request.getNewUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(request));

        verify(userRepository).existsByUsernameAndDeletedIsFalse(request.getNewUsername());
        verify(userRepository, never()).findByUsernameAndDeletedIsFalse(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowUserNotFoundException_whenOldUsernameNotFound() {
        // Arrange
        UpdateAuthUserRequest request = new UpdateAuthUserRequest("oldUsername", "newUsername");

        when(userRepository.existsByUsernameAndDeletedIsFalse(request.getNewUsername())).thenReturn(false);
        when(userRepository.findByUsernameAndDeletedIsFalse(request.getOldUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(request));

        verify(userRepository).existsByUsernameAndDeletedIsFalse(request.getNewUsername());
        verify(userRepository).findByUsernameAndDeletedIsFalse(request.getOldUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUsername_success() throws RedisOperationException {
        // Arrange
        String username = "username";
        User user = new User(1L, "username", "password", true, null, null, null, List.of(new Role("ADMIN")), List.of(new Token()), false);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.of(user));
        userService.deleteUser(username);

        // Assert
        assertTrue(user.isDeleted());
    }

    @Test
    void deleteUsername_userNotFound() {
        // Arrange
        String username = "username";

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(username));
    }

    @Test
    void restoreUsername_success() throws RedisOperationException, UserNotFoundException {
        // Arrange
        String username = "username";
        User user = new User(1L, "username", "password", true, null, null, null, List.of(new Role("ADMIN")), List.of(new Token()), false);

        when(userRepository.findByUsernameAndDeletedIsTrue(username)).thenReturn(Optional.of(user));
        userService.restoreUser(username);

        // Assert
        assertFalse(user.isDeleted());
    }

    @Test
    void restoreUsername_userNotFound() {
        // Arrange
        String username = "username";

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(username));
    }

    @Test
    void addRole_success() throws RoleNotFoundException, UserNotFoundException {
        // Arrange
        String username = "testUser";
        String role = "TECHNICIAN";
        User user = new User(1L, "testUser", "password", true, null, null, null, new ArrayList<>(), List.of(new Token()), false);
        Role adminRole = new Role(1L, "ADMIN", "", null);

        UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, role);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(role)).thenReturn(Optional.of(adminRole));

        userService.addRole(updateAuthUserRoleRequest);

        // Assert
        assertTrue(user.getRoles().contains(adminRole));
        verify(userRepository).save(user);
    }

    @Test
    void addRole_userNotFound() {
        // Arrange
        String username = "nonExistingUser";
        String role = "ADMIN";
        UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, role);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.addRole(updateAuthUserRoleRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRole_roleNotFound() {
        // Arrange
        String username = "testUser";
        String role = "NON_EXISTING_ROLE";
        User user = new User(1L, "testUser", "password", true, null, null, null, new ArrayList<>(), List.of(new Token()), false);

        UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, role);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(role)).thenReturn(Optional.empty());

        // Assert
        assertThrows(RoleNotFoundException.class, () -> userService.addRole(updateAuthUserRoleRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeRole_success() throws RoleNotFoundException, UserNotFoundException {
        // Arrange
        String username = "testUser";
        String role = "ADMIN";
        Role adminRole = new Role(1L, "ADMIN", "", null);
        User user = new User(1L, "testUser", "password", true, null, null, null, new ArrayList<>(List.of(adminRole)), List.of(new Token()), false);

        UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, role);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(role)).thenReturn(Optional.of(adminRole));

        userService.removeRole(updateAuthUserRoleRequest);

        // Assert
        assertFalse(user.getRoles().contains(adminRole));
        verify(userRepository).save(user);
    }

    @Test
    void removeRole_userNotFound() {
        // Arrange
        String username = "nonExistingUser";
        String role = "ADMIN";
        UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, role);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.empty());

        // Assert
        assertThrows(UserNotFoundException.class, () -> userService.removeRole(updateAuthUserRoleRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeRole_roleNotFound() {
        // Arrange
        String username = "testUser";
        String role = "NON_EXISTING_ROLE";
        Role adminRole = new Role(1L, "ADMIN", "", null);
        User user = new User(1L, "testUser", "password", true, null, null, null, new ArrayList<>(List.of(adminRole)), List.of(new Token()), false);

        UpdateAuthUserRoleRequest updateAuthUserRoleRequest = new UpdateAuthUserRoleRequest(username, role);

        when(userRepository.findByUsernameAndDeletedIsFalse(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(role)).thenReturn(Optional.empty());

        // Assert
        assertThrows(RoleNotFoundException.class, () -> userService.removeRole(updateAuthUserRoleRequest));
        verify(userRepository, never()).save(any(User.class));
    }
}
