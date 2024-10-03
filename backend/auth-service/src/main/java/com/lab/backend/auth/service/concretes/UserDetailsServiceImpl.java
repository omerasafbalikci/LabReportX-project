package com.lab.backend.auth.service.concretes;

import com.lab.backend.auth.entity.User;
import com.lab.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link UserDetailsService} interface.
 * This class is responsible for retrieving user information from the database
 * and converting it to a Spring Security {@link UserDetails} object.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Log4j2
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads the user details for a given username by querying the database.
     * If the user is found and not marked as deleted, the user's roles are mapped to authorities
     * and returned as a {@link UserDetails} object for authentication.
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated {@link UserDetails} object (never null)
     * @throws UsernameNotFoundException if the user could not be found or is deleted
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.trace("Entering loadUserByUsername with username: {}", username);
        User user = this.userRepository.findByUsernameAndDeletedIsFalse(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        log.info("User found: {}", user.getUsername());
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = "ROLE_" + role.getName();
                    log.debug("Mapping role {} to authority {}", role.getName(), roleName);
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toSet());
        log.trace("User authorities: {}", authorities);
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
        log.trace("Returning userDetails for username: {}", user.getUsername());
        return userDetails;
    }
}
