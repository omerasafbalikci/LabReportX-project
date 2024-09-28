package com.lab.backend.report.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportRequest {
    @NotBlank(message = "TC must not be blank")
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Invalid TC number")
    private String patientTrIdNumber;
    @NotBlank(message = "Diagnosis title must not be blank")
    private String diagnosisTitle;
    @NotBlank(message = "Diagnosis title must not be blank")
    private String diagnosisDetails;
}
