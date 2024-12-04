package com.lab.backend.report.repository;

import com.lab.backend.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link Report} entities.
 * Extends JpaRepository for standard CRUD operations and JpaSpecificationExecutor for dynamic queries.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    /**
     * Finds a {@link Report} by its ID, ensuring that it is not marked as deleted.
     *
     * @param id the ID of the report to find
     * @return an Optional containing the found Report if it exists and is not deleted, or an empty Optional otherwise
     */
    Optional<Report> findByIdAndDeletedFalse(Long id);

    /**
     * Finds a {@link Report} by its ID, ensuring that it is marked as deleted.
     *
     * @param id the ID of the report to find
     * @return an Optional containing the found Report if it exists and is deleted, or an empty Optional otherwise
     */
    Optional<Report> findByIdAndDeletedTrue(Long id);

    /**
     * Retrieves a list of all file numbers from reports that are not marked as deleted.
     *
     * @return a list of file numbers from reports that are not deleted
     */
    @Query("SELECT r.fileNumber FROM Report r WHERE r.deleted = false")
    List<String> findAllFileNumberAndDeletedFalse();

    /**
     * Counts the number of reports created between the specified start and end times.
     *
     * @param startDate the start of the date range
     * @param endDate   the end of the date range
     * @return the number of reports created in the specified range
     */
    Long countByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
