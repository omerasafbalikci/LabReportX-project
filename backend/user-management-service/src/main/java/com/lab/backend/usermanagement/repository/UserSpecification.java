package com.lab.backend.usermanagement.repository;

import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import jakarta.persistence.criteria.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * UserSpecification is a class that implements {@link Specification} for the {@link User} entity.
 * It is responsible for dynamically building SQL query predicates based on the provided filtering
 * criteria.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String hospitalId;
    private final String email;
    private final String role;
    private final String gender;
    private final Boolean deleted;

    /**
     * Constructs a {@link Predicate} based on the filtering criteria provided.
     *
     * @param root            the root
     * @param query           the criteria query
     * @param criteriaBuilder the criteria builder
     * @return a {@link Predicate} representing the filtering conditions
     */
    @Override
    public Predicate toPredicate(@NonNull Root<User> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (firstName != null && !firstName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%"));
        }
        if (lastName != null && !lastName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%"));
        }
        if (username != null && !username.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("username"), username));
        }
        if (hospitalId != null && !hospitalId.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("hospitalId"), hospitalId));
        }
        if (email != null && !email.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
        }
        if (role != null && !role.isEmpty()) {
            Join<User, Role> rolesJoin = root.join("roles");
            predicates.add(criteriaBuilder.equal(rolesJoin, Role.valueOf(role)));
        }
        if (gender != null && !gender.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
        }
        if (deleted != null) {
            predicates.add(criteriaBuilder.equal(root.get("deleted"), deleted));
        } else {
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
