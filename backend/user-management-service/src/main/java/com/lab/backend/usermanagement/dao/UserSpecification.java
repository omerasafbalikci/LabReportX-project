package com.lab.backend.usermanagement.dao;

import com.lab.backend.usermanagement.entity.Role;
import com.lab.backend.usermanagement.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {
    private final String firstName;
    private final String lastName;
    private final String username;
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
        if (email != null && !email.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
        }
        if (role != null && !role.isEmpty()) {
            Predicate rolePredicate = criteriaBuilder.exists(
                    query.subquery(Role.class)
                            .select(query.from(Role.class))
                            .where(
                                    criteriaBuilder.equal(root.join("roles").get("name"), role)
                            )
            );
            predicates.add(rolePredicate);
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