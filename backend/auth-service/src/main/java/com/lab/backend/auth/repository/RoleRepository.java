package com.lab.backend.auth.repository;

import com.lab.backend.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing and managing {@link Role} entities in the database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Finds a role by its name.
     *
     * @param name the name of the role
     * @return an {@link Optional} containing the role if found, otherwise empty
     */
    Optional<Role> findByName(String name);
}
