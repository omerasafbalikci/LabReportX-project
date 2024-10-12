package com.lab.backend.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for report used as response.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetReportResponse implements Serializable {
    private Long id;
    private String fileNumber;
    private String patientTrIdNumber;
    private String diagnosisTitle;
    private String diagnosisDetails;
    private String date;
    private String photoPath;
    private String technicianUsername;
}
