package com.lab.backend.patient.dao;

import com.lab.backend.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing patients table in database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    Optional<Patient> findByIdAndDeletedFalse(Long id);

    Optional<Patient> findByIdAndDeletedTrue(Long id);

    boolean existsByTrIdNumberAndDeletedIsFalse(String trIdNumber);
}
