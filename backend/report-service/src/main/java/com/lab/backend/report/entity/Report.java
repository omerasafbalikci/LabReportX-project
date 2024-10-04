package com.lab.backend.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Report class represents a report entity in the database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Entity
@Table(name = "reports",
        indexes = {
                @Index(name = "idx_file_number_date_patient", columnList = "file_number, date, patient_tc")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "file_number", length = 8)
    private String fileNumber;

    @Column(name = "patient_tc", length = 11)
    private String patientTrIdNumber;

    @Column(name = "diagnosis_title", columnDefinition = "TEXT")
    private String diagnosisTitle;

    @Column(name = "diagnosis_details", columnDefinition = "TEXT")
    private String diagnosisDetails;

    @Column(name = "date")
    private Date date;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(name = "technician_username")
    private String technicianUsername;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
