package com.lab.backend.usermanagement.repository;

import com.lab.backend.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository is the repository interface for performing CRUD operations and
 * custom queries on the {@link User} entity. It extends {@link JpaRepository}
 * for standard database operations and {@link JpaSpecificationExecutor} for supporting
 * filtering and sorting with specifications.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Finds a user by its ID, but only if the user is not marked as deleted.
     *
     * @param id the ID of the user
     * @return an {@link Optional} containing the user if found and not deleted, otherwise empty
     */
    Optional<User> findByIdAndDeletedFalse(Long id);

    /**
     * Finds a user by its ID, but only if the user is marked as deleted.
     *
     * @param id the ID of the user
     * @return an {@link Optional} containing the user if found and deleted, otherwise empty
     */
    Optional<User> findByIdAndDeletedTrue(Long id);

    /**
     * Finds a user by its email, but only if the user is not marked as deleted.
     *
     * @param email the email of the user
     * @return an {@link Optional} containing the user if found and not deleted, otherwise empty
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * Finds a user by its username, but only if the user is not marked as deleted.
     *
     * @param username the username of the user
     * @return an {@link Optional} containing the user if found and not deleted, otherwise empty
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);

    /**
     * Retrieves all unique hospital IDs for users that are not marked as deleted.
     *
     * @return a {@link List} of hospital IDs for non-deleted users
     */
    @Query("SELECT u.hospitalId FROM User u WHERE u.deleted = false")
    List<String> findAllHospitalIdAndDeletedFalse();

    /**
     * Checks if a user with the specified username exists and is not marked as deleted.
     *
     * @param username the username of the user
     * @return {@code true} if the user exists and is not deleted, {@code false} otherwise
     */
    boolean existsByUsernameAndDeletedIsFalse(String username);

    /**
     * Checks if a user with the specified email exists and is not marked as deleted.
     *
     * @param email the email of the user
     * @return {@code true} if the user exists and is not deleted, {@code false} otherwise
     */
    boolean existsByEmailAndDeletedIsFalse(String email);
}
