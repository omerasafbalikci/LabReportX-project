package com.lab.backend.patient.repository;

import com.lab.backend.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for accessing patients table in database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    Optional<Patient> findByIdAndDeletedFalse(Long id);

    Optional<Patient> findByIdAndDeletedTrue(Long id);

    Optional<Patient> findByTrIdNumberAndDeletedFalse(String trIdNumber);

    @Query("SELECT p.chronicDiseases FROM Patient p WHERE p.id = :id AND p.deleted = false")
    Set<String> findChronicDiseasesByIdAndDeletedFalse(@Param("id") Long id);
}
