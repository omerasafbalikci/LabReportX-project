package com.lab.backend.auth.repository;

import com.lab.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing and managing {@link User} entities in the database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their username where the user is not marked as deleted.
     *
     * @param username the username of the user
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    Optional<User> findByUsernameAndDeletedIsFalse(String username);

    /**
     * Finds a user by their username where the user is marked as deleted.
     *
     * @param username the username of the user
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    Optional<User> findByUsernameAndDeletedIsTrue(String username);

    /**
     * Finds a user by their email verification token where the user is not marked as deleted.
     *
     * @param emailVerificationToken the email verification token of the user
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    Optional<User> findByEmailVerificationTokenAndDeletedIsFalse(String emailVerificationToken);

    /**
     * Finds a user by their reset token where the user is not marked as deleted.
     *
     * @param token the reset token of the user
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    Optional<User> findByResetTokenAndDeletedIsFalse(String token);

    /**
     * Checks if a user with the specified username exists and is not marked as deleted.
     *
     * @param username the username to check
     * @return true if the user exists and is not deleted, otherwise false
     */
    boolean existsByUsernameAndDeletedIsFalse(String username);
}
