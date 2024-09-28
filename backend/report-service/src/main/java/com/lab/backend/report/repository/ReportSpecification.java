package com.lab.backend.report.repository;

import com.lab.backend.report.entity.Report;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportSpecification implements Specification<Report> {
    private final String fileNumber;
    private final String patientTrIdNumber;
    private final String diagnosisTitle;
    private final String diagnosisDetails;
    private final String date;
    private final String photoPath;
    private String technicianUsername;
    private final Boolean deleted;

    public ReportSpecification(String fileNumber, String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date, String photoPath, String technicianUsername, Boolean deleted) {
        this.fileNumber = fileNumber;
        this.patientTrIdNumber = patientTrIdNumber;
        this.diagnosisTitle = diagnosisTitle;
        this.diagnosisDetails = diagnosisDetails;
        this.date = date;
        this.photoPath = photoPath;
        this.technicianUsername = technicianUsername;
        this.deleted = deleted;
    }

    public ReportSpecification(String fileNumber, String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date, String photoPath, Boolean deleted) {
        this.fileNumber = fileNumber;
        this.patientTrIdNumber = patientTrIdNumber;
        this.diagnosisTitle = diagnosisTitle;
        this.diagnosisDetails = diagnosisDetails;
        this.date = date;
        this.photoPath = photoPath;
        this.deleted = deleted;
    }

    /**
     * Constructs a {@link Predicate} based on the filtering criteria provided.
     *
     * @param root            the root
     * @param query           the criteria query
     * @param criteriaBuilder the criteria builder
     * @return a {@link Predicate} representing the filtering conditions
     */
    @Override
    public Predicate toPredicate(@NonNull Root<Report> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (technicianUsername != null && !technicianUsername.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("technicianUsername"), technicianUsername));
        }
        if (fileNumber != null && !fileNumber.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("fileNumber"), fileNumber));
        }
        if (patientTrIdNumber != null && !patientTrIdNumber.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("patientTrIdNumber"), patientTrIdNumber));
        }
        if (diagnosisTitle != null && !diagnosisTitle.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("diagnosisTitle"), "%" + diagnosisTitle + "%"));
        }
        if (diagnosisDetails != null && !diagnosisDetails.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("diagnosisDetails"), "%" + diagnosisDetails + "%"));
        }
        if (date != null && !date.isEmpty()) {
            List<DateTimeFormatter> formatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            );
            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
                    predicates.add(criteriaBuilder.equal(root.get("date"), dateTime));
                    break;
                } catch (DateTimeParseException ignored) {
                }
            }
        }
        if (photoPath != null && !photoPath.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("photoPath"), "%" + photoPath + "%"));
        }
        if (deleted != null) {
            predicates.add(criteriaBuilder.equal(root.get("deleted"), deleted));
        } else {
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
