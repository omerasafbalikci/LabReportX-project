package com.lab.backend.auth.initializer;

import com.lab.backend.auth.entity.Role;
import com.lab.backend.auth.entity.User;
import com.lab.backend.auth.repository.RoleRepository;
import com.lab.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeSecretaryUser();
        initializeTechnicianUser();
        initializeAdminUser();
        initializeSuperUser();
    }

    private void initializeRoles() {
        List<Role> roles = this.roleRepository.findAll();
        if (roles.isEmpty()) {
            List<Role> roleList = List.of(
                    new Role(1L, "ADMIN", "ADMIN", null),
                    new Role(2L, "SECRETARY", "SECRETARY", null),
                    new Role(3L, "TECHNICIAN", "TECHNICIAN", null)
            );
            this.roleRepository.saveAll(roleList);
        }
    }

    private void initializeSecretaryUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("ozlembalikci")) {
            String encodedPassword = this.passwordEncoder.encode("omerasaf1899");
            Role secretaryRole = this.roleRepository.findByName("SECRETARY").orElseThrow();

            this.userRepository.save(User.builder()
                    .username("ozlembalikci")
                    .password(encodedPassword)
                    .emailVerified(true)
                    .emailVerificationToken(null)
                    .resetToken(null)
                    .resetTokenExpiration(null)
                    .roles(Set.of(secretaryRole))
                    .build());
        }
    }

    private void initializeTechnicianUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("kadircanbalikci")) {
            String encodedPassword = this.passwordEncoder.encode("omerasaf1899");
            Role technicianRole = this.roleRepository.findByName("TECHNICIAN").orElseThrow();

            this.userRepository.save(User.builder()
                    .username("kadircanbalikci")
                    .password(encodedPassword)
                    .emailVerified(true)
                    .emailVerificationToken(null)
                    .resetToken(null)
                    .resetTokenExpiration(null)
                    .roles(Set.of(technicianRole))
                    .build());
        }
    }

    private void initializeAdminUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("omerasafbalikci")) {
            String encodedPassword = this.passwordEncoder.encode("omerasaf1899");
            Role adminRole = this.roleRepository.findByName("ADMIN").orElseThrow();

            this.userRepository.save(User.builder()
                    .username("omerasafbalikci")
                    .password(encodedPassword)
                    .emailVerified(true)
                    .emailVerificationToken(null)
                    .resetToken(null)
                    .resetTokenExpiration(null)
                    .roles(Set.of(adminRole))
                    .build());
        }
    }

    private void initializeSuperUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("super")) {
            String encodedPassword = this.passwordEncoder.encode("omerasaf1899");
            Role adminRole = this.roleRepository.findByName("ADMIN").orElseThrow();
            Role technicianRole = this.roleRepository.findByName("TECHNICIAN").orElseThrow();
            Role secretaryRole = this.roleRepository.findByName("SECRETARY").orElseThrow();

            this.userRepository.save(User.builder()
                    .username("super")
                    .password(encodedPassword)
                    .emailVerified(true)
                    .emailVerificationToken(null)
                    .resetToken(null)
                    .resetTokenExpiration(null)
                    .roles(Set.of(adminRole, technicianRole, secretaryRole))
                    .build());
        }
    }
}
