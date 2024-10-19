package com.lab.backend.auth.service.concretes;

import com.lab.backend.auth.entity.Role;
import com.lab.backend.auth.entity.User;
import com.lab.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    private User user;

    @BeforeEach
    void setUp() {
        Role roles = new Role("USER");
        this.user = new User();
        this.user.setUsername("testuser");
        this.user.setPassword("password");
        this.user.setRoles(List.of(roles));
    }

    @Test
    void loadUserByUsername_withExistingUsername_shouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsernameAndDeletedIsFalse("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = this.userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        Set<GrantedAuthority> expectedAuthorities = this.user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
        assertEquals(expectedAuthorities, userDetails.getAuthorities());
    }

    @Test
    void loadUserByUsername_withNonExistingUsername_shouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsernameAndDeletedIsFalse("nonexistentuser")).thenReturn(Optional.empty());

        // Act
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistentuser"));

        // Assert
        assertEquals("User not found with username: nonexistentuser", exception.getMessage());
    }
}
