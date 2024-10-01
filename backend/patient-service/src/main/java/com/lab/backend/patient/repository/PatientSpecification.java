package com.lab.backend.patient.repository;

import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.utilities.exceptions.UnexpectedException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final String lastPatientRegistrationTime;
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
    public Predicate toPredicate(@NonNull Root<Patient> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (firstName != null && !lastName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%"));
        }
        if (lastName != null && !lastName.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%"));
        }
        if (trIdNumber != null && !trIdNumber.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("trIdNumber"), trIdNumber));
        }
        if (birthDate != null && !birthDate.isEmpty()) {
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
        if (lastPatientRegistrationTime != null && !lastPatientRegistrationTime.isEmpty()) {
            List<DateTimeFormatter> formatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            );
            boolean parsed = false;
            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(lastPatientRegistrationTime, formatter);
                    predicates.add(criteriaBuilder.equal(root.get("lastPatientRegistrationTime"), dateTime));
                    parsed = true;
                    break;
                } catch (DateTimeParseException ignored) {
                }
            }
            if (!parsed) {
                throw new UnexpectedException("Invalid date format: " + lastPatientRegistrationTime);
            }
        }
        if (deleted != null) {
            predicates.add(criteriaBuilder.equal(root.get("deleted"), deleted));
        } else {
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
