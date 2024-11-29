package com.lab.backend.patient.repository;

import com.lab.backend.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for accessing patients table in database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    /**
     * Finds a patient by their ID if the patient is not marked as deleted.
     *
     * @param id the ID of the patient
     * @return an {@link Optional} containing the patient if found, otherwise empty
     */
    Optional<Patient> findByIdAndDeletedFalse(Long id);

    /**
     * Finds a patient by their ID if the patient is marked as deleted.
     *
     * @param id the ID of the patient
     * @return an {@link Optional} containing the patient if found, otherwise empty
     */
    Optional<Patient> findByIdAndDeletedTrue(Long id);

    /**
     * Finds a patient by their TR ID number if the patient is not marked as deleted.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return an {@link Optional} containing the patient if found, otherwise empty
     */
    Optional<Patient> findByTrIdNumberAndDeletedFalse(String trIdNumber);

    /**
     * Checks if a patient exists by their TR ID number if the patient is not marked as deleted.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return {@code true} if a patient exists with the given TR ID number and is not deleted, {@code false} otherwise
     */
    boolean existsByTrIdNumberAndDeletedIsFalse(String trIdNumber);

    Long countByRegistrationTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves the chronic diseases of a patient by their ID if the patient is not marked as deleted.
     *
     * @param id the ID of the patient
     * @return a {@link Set} containing the chronic diseases of the patient, or an empty set if the patient is not found
     */
    @Query("SELECT p.chronicDiseases FROM Patient p WHERE p.id = :id AND p.deleted = false")
    Set<String> findChronicDiseasesByIdAndDeletedFalse(Long id);
}
