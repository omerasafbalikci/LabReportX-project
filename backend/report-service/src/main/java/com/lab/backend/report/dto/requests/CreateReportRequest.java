package com.lab.backend.report.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for report used as input.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportRequest {
    @NotBlank(message = "Diagnosis title must not be blank")
    private String diagnosisTitle;
    @NotBlank(message = "Diagnosis title must not be blank")
    private String diagnosisDetails;
}
