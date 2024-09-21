package com.lab.backend.usermanagement.initializer;

import com.lab.backend.usermanagement.entity.Gender;
import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import com.lab.backend.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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
                    .email("blkc.omerasaff@gmail.com")
                    .roles(Set.of(Role.SECRETARY))
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
                    .email("blkc.omerasaff@gmail.com")
                    .roles(Set.of(Role.TECHNICIAN))
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
                    .roles(Set.of(Role.ADMIN))
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
                    .email("blkc.omerasaff@gmail.com")
                    .roles(Set.of(Role.SECRETARY, Role.TECHNICIAN, Role.ADMIN))
                    .gender(Gender.MALE)
                    .build());
        }
    }
}
