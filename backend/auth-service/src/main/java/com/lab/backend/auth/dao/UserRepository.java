package com.lab.backend.auth.dao;

import com.lab.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndDeletedIsFalse(String username);

    Optional<User> findByResetTokenAndDeletedIsFalse(String token);

    boolean existsByUsernameAndDeletedIsFalse(String username);
}
