package com.lab.backend.usermanagement.dao;

import com.lab.backend.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByIdAndDeletedTrue(Long id);

    boolean existsByUsernameAndDeletedIsFalse(String username);

    boolean existsByEmailAndDeletedIsFalse(String email);
}
