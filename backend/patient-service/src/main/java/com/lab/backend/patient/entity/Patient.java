package com.lab.backend.patient.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "tr_id_number", nullable = false, length = 11)
    private String trIdNumber;

    @Column(name = "birth_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
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
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chronic_diseases", joinColumns = @JoinColumn(name = "patient_id"))
    @Builder.Default
    private Set<String> chronicDiseases = new HashSet<>();

    @Column(name = "last_patient_registration_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime lastPatientRegistrationTime;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
