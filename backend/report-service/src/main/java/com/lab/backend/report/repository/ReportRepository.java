package com.lab.backend.report.repository;

import com.lab.backend.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    Optional<Report> findByIdAndDeletedFalse(Long id);

    Optional<Report> findByIdAndDeletedTrue(Long id);

    @Query("SELECT r.fileNumber FROM Report r WHERE r.deleted = false")
    List<String> findAllFileNumberAndDeletedFalse();
}
