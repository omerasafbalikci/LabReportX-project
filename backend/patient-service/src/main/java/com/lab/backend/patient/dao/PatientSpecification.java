package com.lab.backend.patient.dao;

import com.lab.backend.patient.entity.Patient;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for filtering patients based on various criteria.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RequiredArgsConstructor
public class PatientSpecification implements Specification<Patient> {
    private final String firstName;
    private final String lastName;
    private final String trIdNumber;
    private final String birthDate;
    private final String gender;
    private final String bloodType;
    private final String phoneNumber;
    private final String email;
    private final String chronicDisease;
    private final String lastModifiedDate;
    private final Boolean deleted;

    /**
     * Constructs a {@link Predicate} based on the filtering criteria provided.
     *
     * @param root the root
     * @param query the criteria query
     * @param criteriaBuilder the criteria builder
     * @return a {@link Predicate} representing the filtering conditions
     */
    @Override
    public Predicate toPredicate(@NonNull Root<Patient> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (firstName != null && !firstName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%"));
        }
        if (lastName != null && !lastName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%"));
        }
        if (trIdNumber != null && !trIdNumber.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("trIdNumber"), trIdNumber));
        }
        if (birthDate != null) {
            LocalDate parsedBirthDate = LocalDate.parse(birthDate);
            predicates.add(criteriaBuilder.equal(root.get("birthDate"), parsedBirthDate));
        }
        if (gender != null && !gender.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
        }
        if (bloodType != null && !bloodType.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("bloodType"), bloodType));
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("phoneNumber"), "%" + phoneNumber + "%"));
        }
        if (email != null && !email.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
        }
        if (chronicDisease != null && !chronicDisease.isEmpty()) {
            predicates.add(criteriaBuilder.isMember(chronicDisease, root.get("chronicDiseases")));
        }
        if (lastModifiedDate != null) {
            LocalDateTime parsedUpdatedDate = LocalDateTime.parse(lastModifiedDate);
            predicates.add(criteriaBuilder.equal(root.get("updatedDate"), parsedUpdatedDate));
        }
        if (deleted != null) {
            predicates.add(criteriaBuilder.equal(root.get("deleted"), deleted));
        } else {
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
