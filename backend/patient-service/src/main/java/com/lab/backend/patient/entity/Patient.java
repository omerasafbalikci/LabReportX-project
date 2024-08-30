package com.lab.backend.patient.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Patient class represents a patient entity in the database.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_tr_id_number", columnList = "tr_id_number")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "tr_id_number", unique = true, nullable = false, length = 11)
    private String trIdNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "blood_type")
    private BloodType bloodType;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "chronic_diseases")
    @ElementCollection
    @CollectionTable(name = "chronic_diseases", joinColumns = @JoinColumn(name = "patient_id"))
    private List<String> chronicDiseases;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "deleted", nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}
