package com.lab.backend.usermanagement.initializer;

import com.lab.backend.usermanagement.entity.Gender;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initializer class is responsible for creating default users upon application startup.
 * This class implements the {@link CommandLineRunner} interface and ensures that specific users
 * with predefined roles are initialized in the database if they do not already exist.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeSecretaryUser();
        initializeTechnicianUser();
        initializeAdminUser();
        initializeSuperUser();
    }

    private void initializeSecretaryUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("ozlembalikci")) {
            this.userRepository.save(User.builder()
                    .firstName("Özlem")
                    .lastName("Balıkçı")
                    .username("ozlembalikci")
                    .hospitalId("ABCDEF1")
                    .email("blkc.ozlem@gmail.com")
                    .roles(List.of(Role.SECRETARY))
                    .gender(Gender.FEMALE)
                    .build());
        }
    }

    private void initializeTechnicianUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("kadircanbalikci")) {
            this.userRepository.save(User.builder()
                    .firstName("Kadir Can")
                    .lastName("Balıkçı")
                    .username("kadircanbalikci")
                    .hospitalId("ABCDEF2")
                    .email("blkc.kadir@gmail.com")
                    .roles(List.of(Role.TECHNICIAN))
                    .gender(Gender.MALE)
                    .build());
        }
    }

    private void initializeAdminUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("omerasafbalikci")) {
            this.userRepository.save(User.builder()
                    .firstName("Ömer Asaf")
                    .lastName("Balıkçı")
                    .username("omerasafbalikci")
                    .hospitalId("ABCDEF3")
                    .email("blkc.omerasaff@gmail.com")
                    .roles(List.of(Role.ADMIN))
                    .gender(Gender.MALE)
                    .build());
        }
    }

    private void initializeSuperUser() {
        if (!userRepository.existsByUsernameAndDeletedIsFalse("super")) {
            this.userRepository.save(User.builder()
                    .firstName("Super")
                    .lastName("Super")
                    .username("super")
                    .hospitalId("ABCDEF4")
                    .email("super@gmail.com")
                    .roles(List.of(Role.SECRETARY, Role.TECHNICIAN, Role.ADMIN))
                    .gender(Gender.MALE)
                    .build());
        }
    }
}
