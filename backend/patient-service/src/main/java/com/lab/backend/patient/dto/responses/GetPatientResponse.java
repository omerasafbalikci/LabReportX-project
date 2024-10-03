package com.lab.backend.patient.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    private String birthDate;
    private String gender;
    private String bloodType;
    private String phoneNumber;
    private String email;
    private String lastPatientRegistrationTime;
}
