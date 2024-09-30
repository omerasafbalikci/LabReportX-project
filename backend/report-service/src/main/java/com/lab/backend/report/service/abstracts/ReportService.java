package com.lab.backend.report.service.abstracts;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface ReportService {
    GetReportResponse getReportById(Long id);

    PagedResponse<GetReportResponse> getAllReportsFilteredAndSorted(int page, int size, String sortBy, String direction, String fileNumber,
                                                                    String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date,
                                                                    String photoPath, Boolean deleted);

    PagedResponse<GetReportResponse> getReportsByTechnician(String username, int page, int size, String sortBy, String direction, String fileNumber,
                                                            String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date,
                                                            String photoPath, Boolean deleted);

    String checkTrIdNumber(String username, String trIdNumber);

    GetReportResponse addReport(String username, CreateReportRequest createReportRequest);

    GetReportResponse updateReport(UpdateReportRequest updateReportRequest);

    void deleteReport(Long id);

    GetReportResponse restoreReport(Long id);

    void addPhoto(Long reportId, MultipartFile photo);

    byte[] getPhoto(Long reportId);

    void deletePhoto(Long reportId);

    Mono<byte[]> getReportPdf(Long reportId);

    byte[] getPrescription(String username, Long reportId);

    void sendPrescription(String username);
}
