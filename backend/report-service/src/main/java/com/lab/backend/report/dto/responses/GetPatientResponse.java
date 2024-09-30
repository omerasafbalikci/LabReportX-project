package com.lab.backend.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPatientResponse {
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
