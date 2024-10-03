package com.lab.backend.auth.repository;

import com.lab.backend.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and managing {@link Token} entities in the database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    /**
     * Retrieves all valid tokens (not logged out) associated with a specific user.
     *
     * @param userId the ID of the user
     * @return a list of valid tokens for the user
     */
    @Query("SELECT t FROM tokens t WHERE t.user.id = :userId AND t.loggedOut = false")
    List<Token> findAllValidTokensByUser(@Param("userId") Long userId);

    /**
     * Finds a token by its string value.
     *
     * @param token the token string
     * @return an {@link Optional} containing the token if found, otherwise empty
     */
    Optional<Token> findByToken(String token);
}
