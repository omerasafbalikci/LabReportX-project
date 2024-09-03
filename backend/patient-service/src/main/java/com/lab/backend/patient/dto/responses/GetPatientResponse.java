package com.lab.backend.patient.dto.responses;

import com.lab.backend.patient.entity.BloodType;
import com.lab.backend.patient.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for patient used as response.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPatientResponse implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String trIdNumber;
    private LocalDate birthDate;
    private Gender gender;
    private BloodType bloodType;
    private String phoneNumber;
    private String email;
    private List<String> chronicDiseases;
    private LocalDateTime lastModifiedDate;
}
