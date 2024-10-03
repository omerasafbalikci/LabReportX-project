package com.lab.backend.report.utilities.mappers;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.entity.Report;
import com.lab.backend.report.utilities.FileNumberGenerator;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper class to convert between Report entity and corresponding DTOs.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@AllArgsConstructor
public class ReportMapper {
    @Autowired
    private FileNumberGenerator fileNumberGenerator;

    public Report toReport(CreateReportRequest request) {
        if (request == null) {
            return null;
        }
        Report report = new Report();
        report.setFileNumber(this.fileNumberGenerator.generateUniqueFileNumber());
        report.setDiagnosisTitle(request.getDiagnosisTitle());
        report.setDiagnosisDetails(request.getDiagnosisDetails());
        report.setDate(LocalDateTime.now());
        return report;
    }

    public GetReportResponse toGetReportResponse(Report report) {
        if (report == null) {
            return null;
        }
        return new GetReportResponse(
                report.getId(),
                report.getFileNumber(),
                report.getPatientTrIdNumber(),
                report.getDiagnosisTitle(),
                report.getDiagnosisDetails(),
                report.getDate().toString(),
                report.getPhotoPath(),
                report.getTechnicianUsername()
        );
    }
}
