package com.lab.backend.report.service.abstracts;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing report entities and their associated operations.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface ReportService {
    /**
     * Retrieves a report by its ID.
     *
     * @param id the ID of the report to retrieve
     * @return the {@link GetReportResponse} containing the report details
     */
    GetReportResponse getReportById(Long id);

    /**
     * Sends weekly report statistics by calculating the number of reports created in the past 7 days.
     * The statistics are sent to a Kafka topic.
     */
    void sendWeeklyReportStats();

    /**
     * Retrieves all reports with optional filtering and sorting.
     *
     * @param page               the page number to retrieve
     * @param size               the number of records per page
     * @param sortBy             the field to sort by
     * @param direction          the sort direction (asc or desc)
     * @param fileNumber         optional file number filter
     * @param patientTrIdNumber  optional patient TR ID number filter
     * @param diagnosisTitle     optional diagnosis title filter
     * @param diagnosisDetails   optional diagnosis details filter
     * @param date               optional date filter
     * @param photoPath          optional photo path filter
     * @param technicianUsername the username of the technician
     * @param deleted            optional filter to include/exclude deleted reports
     * @return a paged response containing the list of filtered and sorted reports
     */
    PagedResponse<GetReportResponse> getAllReportsFilteredAndSorted(int page, int size, String sortBy, String direction, String fileNumber,
                                                                    String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date,
                                                                    String photoPath, String technicianUsername, Boolean deleted);

    /**
     * Retrieves reports assigned to a specific technician with optional filtering and sorting.
     *
     * @param username          the username of the technician
     * @param page              the page number to retrieve
     * @param size              the number of records per page
     * @param sortBy            the field to sort by
     * @param direction         the sort direction (asc or desc)
     * @param fileNumber        optional file number filter
     * @param patientTrIdNumber optional patient TR ID number filter
     * @param diagnosisTitle    optional diagnosis title filter
     * @param diagnosisDetails  optional diagnosis details filter
     * @param date              optional date filter
     * @param photoPath         optional photo path filter
     * @param deleted           optional filter to include/exclude deleted reports
     * @return a paged response containing the technician's reports
     */
    PagedResponse<GetReportResponse> getReportsByTechnician(String username, int page, int size, String sortBy, String direction, String fileNumber,
                                                            String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date,
                                                            String photoPath, Boolean deleted);

    /**
     * Validates a TR ID number.
     *
     * @param username   the username of the technician
     * @param trIdNumber the TR ID number to validate
     * @return a validation message
     */
    String checkTrIdNumber(String username, String trIdNumber);

    /**
     * Adds a new report.
     *
     * @param username            the username of the technician adding the report
     * @param createReportRequest the request object containing report details
     * @return the added report as a {@link GetReportResponse}
     */
    GetReportResponse addReport(String username, CreateReportRequest createReportRequest);

    /**
     * Updates an existing report.
     *
     * @param username            the username of the technician updating the report
     * @param updateReportRequest the request object containing updated report details
     * @return the updated report as a {@link GetReportResponse}
     */
    GetReportResponse updateReport(String username, UpdateReportRequest updateReportRequest);

    /**
     * Deletes a report by its ID.
     *
     * @param username the username of the technician performing the deletion
     * @param id       the ID of the report to delete
     */
    void deleteReport(String username, Long id);

    /**
     * Restores a deleted report by its ID.
     *
     * @param username the username of the technician restoring the report
     * @param id       the ID of the report to restore
     * @return the restored report as a {@link GetReportResponse}
     */
    GetReportResponse restoreReport(String username, Long id);

    /**
     * Adds a photo to a report.
     *
     * @param username the username of the technician adding the photo
     * @param reportId the ID of the report to which the photo will be added
     * @param photo    the photo file to upload
     */
    void addPhoto(String username, Long reportId, MultipartFile photo);

    /**
     * Retrieves a photo associated with a report.
     *
     * @param username the username of the technician requesting the photo
     * @param reportId the ID of the report whose photo is requested
     * @return the photo data as a byte array
     */
    byte[] getPhoto(String username, Long reportId);

    /**
     * Deletes a photo associated with a report.
     *
     * @param username the username of the technician deleting the photo
     * @param reportId the ID of the report from which the photo will be deleted
     */
    void deletePhoto(String username, Long reportId);

    /**
     * Retrieves the PDF version of a report.
     *
     * @param reportId the ID of the report to retrieve as a PDF
     * @return a Mono containing the PDF data as a byte array
     */
    Mono<byte[]> getReportPdf(Long reportId);

    /**
     * Retrieves the prescription associated with a report.
     *
     * @param username the username of the technician requesting the prescription
     * @param reportId the ID of the report whose prescription is requested
     * @return the prescription data as a byte array
     */
    byte[] getPrescription(String username, Long reportId);

    /**
     * Sends the prescription to the appropriate recipient.
     *
     * @param username the username of the technician sending the prescription
     */
    void sendPrescription(String username);
}
