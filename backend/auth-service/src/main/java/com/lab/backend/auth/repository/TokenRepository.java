package com.lab.backend.auth.repository;

import com.lab.backend.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    @Query("SELECT t FROM tokens t WHERE t.user.id = :userId AND t.loggedOut = false")
    List<Token> findAllValidTokensByUser(@Param("userId") Long userId);
}
