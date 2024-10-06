package com.lab.backend.report.repository;

import com.lab.backend.report.entity.Report;
import com.lab.backend.report.utilities.exceptions.UnexpectedException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Specification for filtering reports based on various criteria.
 * Implements Spring Data JPA's Specification interface for dynamic queries.
 *
 * @author Ömer Asaf BALIKÇI
 */

public class ReportSpecification implements Specification<Report> {
    private final String fileNumber;
    private final String patientTrIdNumber;
    private final String diagnosisTitle;
    private final String diagnosisDetails;
    private final String date;
    private final String photoPath;
    private final String technicianUsername;
    private final Boolean deleted;

    /**
     * Constructor for initializing the specification with all fields.
     *
     * @param fileNumber         the file number of the report
     * @param patientTrIdNumber  the TR ID number of the patient
     * @param diagnosisTitle     the title of the diagnosis
     * @param diagnosisDetails   the details of the diagnosis
     * @param date               the date of the report
     * @param photoPath          the path to the photo associated with the report
     * @param technicianUsername the username of the technician
     * @param deleted            the deletion status of the report
     */
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            try {
                Date d = dateFormat.parse(date);
                predicates.add(criteriaBuilder.equal(root.get("date"), d));
            } catch (ParseException e) {
                throw new UnexpectedException("Error parsing date: " + e);
            }
        }
        if (technicianUsername != null && !technicianUsername.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("technicianUsername"), technicianUsername));
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
