package com.lab.backend.usermanagement.repository;

import com.lab.backend.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByIdAndDeletedTrue(Long id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    @Query("SELECT u.hospitalId FROM User u WHERE u.deleted = false")
    List<String> findAllHospitalIdAndDeletedFalse();

    boolean existsByUsernameAndDeletedIsFalse(String username);

    boolean existsByEmailAndDeletedIsFalse(String email);
}
