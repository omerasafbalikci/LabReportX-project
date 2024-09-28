package com.lab.backend.report.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReportRequest {
    @NotNull(message = "Id must not be null")
    private Long id;
    private String diagnosisTitle;
    private String diagnosisDetails;
}
