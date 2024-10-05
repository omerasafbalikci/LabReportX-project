package com.lab.backend.report.controller;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.service.abstracts.ReportService;
import com.lab.backend.report.utilities.exceptions.InvalidTrIdNumberException;
import com.lab.backend.report.utilities.exceptions.UnexpectedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller class for handling report-related operations.
 * This class provides RESTful endpoints for creating, updating, deleting,
 * retrieving, and managing reports.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Log4j2
public class ReportController {
    private final ReportService reportService;

    /**
     * Retrieves a report by its ID.
     *
     * @param id the ID of the report to retrieve
     * @return the ResponseEntity containing the report details
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<GetReportResponse> getReportById(@PathVariable("id") Long id) {
        log.trace("Entering getReportById method in ReportController class");
        log.info("Retrieving report with ID: {}", id);
        GetReportResponse response = this.reportService.getReportById(id);
        log.trace("Exiting getReportById method in ReportController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all reports filtered and sorted based on given parameters.
     *
     * @param page               the page number to retrieve
     * @param size               the number of reports per page
     * @param sortBy             the field to sort by
     * @param direction          the sort direction (ASC or DESC)
     * @param fileNumber         optional file number filter
     * @param patientTrIdNumber  optional patient TR ID number filter
     * @param diagnosisTitle     optional diagnosis title filter
     * @param diagnosisDetails   optional diagnosis details filter
     * @param date               optional date filter
     * @param photoPath          optional photo path filter
     * @param technicianUsername the username of the technician
     * @param deleted            optional deleted status filter
     * @return the ResponseEntity containing the paginated list of reports
     */
    @GetMapping("/filtered-and-sorted")
    public ResponseEntity<PagedResponse<GetReportResponse>> getAllReportsFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String fileNumber,
            @RequestParam(required = false) String patientTrIdNumber,
            @RequestParam(required = false) String diagnosisTitle,
            @RequestParam(required = false) String diagnosisDetails,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String photoPath,
            @RequestParam(required = false) String technicianUsername,
            @RequestParam(required = false) Boolean deleted
    ) {
        log.trace("Entering getAllReportsFilteredAndSorted method in ReportController class");
        log.info("Retrieving reports filtered and sorted: page={}, size={}, sortBy={}, direction={}, fileNumber={}, patientTrIdNumber={}, diagnosisTitle={}, diagnosisDetails={}, date={}, photoPath={}, technicianUsername={}, deleted={}",
                page, size, sortBy, direction, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, technicianUsername, deleted);
        PagedResponse<GetReportResponse> response = this.reportService.getAllReportsFilteredAndSorted(page, size, sortBy, direction, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, technicianUsername, deleted);
        log.trace("Exiting getAllReportsFilteredAndSorted method in ReportController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all reports for a specific technician.
     *
     * @param username          the username of the technician
     * @param page              the page number to retrieve
     * @param size              the number of reports per page
     * @param sortBy            the field to sort by
     * @param direction         the sort direction (ASC or DESC)
     * @param fileNumber        optional file number filter
     * @param patientTrIdNumber optional patient TR ID number filter
     * @param diagnosisTitle    optional diagnosis title filter
     * @param diagnosisDetails  optional diagnosis details filter
     * @param date              optional date filter
     * @param photoPath         optional photo path filter
     * @param deleted           optional deleted status filter
     * @return the ResponseEntity containing the paginated list of reports
     */
    @GetMapping("/me/filtered-and-sorted")
    public ResponseEntity<PagedResponse<GetReportResponse>> getAllReportsByTechnician(
            @RequestHeader("X-Username") String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String fileNumber,
            @RequestParam(required = false) String patientTrIdNumber,
            @RequestParam(required = false) String diagnosisTitle,
            @RequestParam(required = false) String diagnosisDetails,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String photoPath,
            @RequestParam(required = false) Boolean deleted
    ) {
        log.trace("Entering getAllReportsByTechnician method in ReportController class");
        log.info("Retrieving reports for technician: page={}, size={}, sortBy={}, direction={}, fileNumber={}, patientTrIdNumber={}, diagnosisTitle={}, diagnosisDetails={}, date={}, photoPath={}, username={}, deleted={}",
                page, size, sortBy, direction, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, username, deleted);
        PagedResponse<GetReportResponse> response = this.reportService.getReportsByTechnician(username, page, size, sortBy, direction, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, deleted);
        log.trace("Exiting getAllReportsByTechnician method in ReportController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Checks if a TR ID number is valid for the specified technician.
     *
     * @param username   the username of the technician
     * @param trIdNumber the TR ID number to check
     * @return the ResponseEntity containing the validation message
     */
    @GetMapping("/check-tr-id")
    public ResponseEntity<String> checkTrIdNumber(@RequestHeader("X-Username") String username, @RequestParam String trIdNumber) {
        log.trace("Entering checkTrIdNumber method in ReportController class");
        log.info("Checking TR ID number: {} for user: {}", trIdNumber, username);
        String responseMessage;
        try {
            responseMessage = this.reportService.checkTrIdNumber(username, trIdNumber);
            log.trace("Exiting checkTrIdNumber method in ReportController class");
            return ResponseEntity.ok(responseMessage);
        } catch (InvalidTrIdNumberException e) {
            log.error("Invalid TR ID number: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UnexpectedException e) {
            log.error("Unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /**
     * Adds a new report.
     *
     * @param username            the username of the technician adding the report
     * @param createReportRequest the request object containing report details
     * @return the ResponseEntity containing the created report details
     */
    @PostMapping
    public ResponseEntity<GetReportResponse> addReport(@RequestHeader("X-Username") String username, @RequestBody @Valid CreateReportRequest createReportRequest) {
        log.trace("Entering addReport method in ReportController class");
        log.info("Adding the report by the user: {}", username);
        GetReportResponse response = this.reportService.addReport(username, createReportRequest);
        log.trace("Exiting addReport method in ReportController class");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing report.
     *
     * @param username            the username of the technician
     * @param updateReportRequest the request body containing updated report details
     * @return ResponseEntity containing the updated report response
     */
    @PutMapping
    public ResponseEntity<GetReportResponse> updateReport(@RequestHeader("X-Username") String username, @RequestBody @Valid UpdateReportRequest updateReportRequest) {
        log.trace("Entering updateReport method in ReportController class");
        log.info("Updating the report by the user: {}", username);
        GetReportResponse response = this.reportService.updateReport(username, updateReportRequest);
        log.trace("Exiting updateReport method in ReportController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a report by its ID.
     *
     * @param username the username of the technician
     * @param id       the ID of the report to delete
     * @return ResponseEntity with a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(@RequestHeader("X-Username") String username, @PathVariable("id") Long id) {
        log.trace("Entering deleteReport method in ReportController class");
        log.info("Deleting the report with ID: {} by the user: {}", id, username);
        this.reportService.deleteReport(username, id);
        log.trace("Exiting deleteReport method in ReportController class");
        return ResponseEntity.ok("Report has been successfully deleted.");
    }

    /**
     * Restores a deleted report by its ID.
     *
     * @param username the username of the technician
     * @param id       the ID of the report to restore
     * @return ResponseEntity containing the restored report response
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<GetReportResponse> restoreReport(@RequestHeader("X-Username") String username, @PathVariable("id") Long id) {
        log.trace("Entering restoreReport method in ReportController class");
        log.info("Restoring the report with ID: {} by the user: {}", id, username);
        GetReportResponse response = this.reportService.restoreReport(username, id);
        log.trace("Exiting restoreReport method in ReportController class");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Adds a photo to a report.
     *
     * @param username the username of the technician
     * @param reportId the ID of the report to add the photo to
     * @param photo    the photo to upload
     * @return ResponseEntity containing the result of the upload
     */
    @PostMapping("/photo/{reportId}")
    public ResponseEntity<String> addPhoto(@RequestHeader("X-Username") String username, @PathVariable("reportId") Long reportId, @RequestParam("photo") MultipartFile photo) {
        log.trace("Entering addPhoto method in ReportController class");
        log.info("Uploading photo for report ID: {} by user: {}", reportId, username);
        this.reportService.addPhoto(username, reportId, photo);
        log.trace("Exiting addPhoto method in ReportController class");
        return ResponseEntity.ok("Photo uploaded successfully.");
    }

    /**
     * Retrieves a photo associated with a report.
     *
     * @param username the username of the technician
     * @param reportId the ID of the report
     * @return ResponseEntity containing the photo data
     */
    @GetMapping("/photo/{reportId}")
    public ResponseEntity<byte[]> getPhoto(@RequestHeader("X-Username") String username, @PathVariable("reportId") Long reportId) {
        log.trace("Entering getPhoto method in ReportController class");
        log.info("Fetching photo for report ID: {} by user: {}", reportId, username);
        byte[] photoData = this.reportService.getPhoto(username, reportId);
        log.trace("Exiting getPhoto method in ReportController class");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photoData);
    }

    /**
     * Deletes a photo associated with a report.
     *
     * @param username the username of the technician
     * @param reportId the ID of the report
     * @return ResponseEntity confirming the deletion
     */
    @DeleteMapping("/photo/{reportId}")
    public ResponseEntity<String> deletePhoto(@RequestHeader("X-Username") String username, @PathVariable("reportId") Long reportId) {
        log.trace("Entering deletePhoto method in ReportController class");
        log.info("Deleting photo for report ID: {} by user: {}", reportId, username);
        this.reportService.deletePhoto(username, reportId);
        log.trace("Exiting deletePhoto method in ReportController class");
        return ResponseEntity.ok("Photo deleted successfully.");
    }

    /**
     * Retrieves the PDF report associated with a report ID.
     *
     * @param reportId the ID of the report
     * @return ResponseEntity containing the PDF report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<Mono<byte[]>> getReportPdf(@PathVariable("reportId") Long reportId) {
        log.trace("Entering getReportPdf method in ReportController class");
        log.info("Fetching PDF report for report ID: {}", reportId);
        Mono<byte[]> pdfBytes = this.reportService.getReportPdf(reportId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "report" + reportId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        log.trace("Exiting getReportPdf method in ReportController class");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Retrieves the prescription PDF associated with a report ID.
     *
     * @param username the username of the technician
     * @param reportId the ID of the report
     * @return ResponseEntity containing the prescription PDF
     */
    @GetMapping("/prescription/{reportId}")
    public ResponseEntity<byte[]> getPrescription(@RequestHeader("X-Username") String username, @PathVariable("reportId") Long reportId) {
        log.trace("Entering getPrescription method in ReportController class");
        log.info("Fetching prescription for report ID: {} by user: {}", reportId, username);
        byte[] pdfBytes = this.reportService.getPrescription(username, reportId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "patient" + "-prescription_" + dateFormat.format(new Date()) + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        log.trace("Exiting getPrescription method in ReportController class");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Sends the prescription to the specified recipient.
     *
     * @param username the username of the technician
     * @return ResponseEntity confirming the sending of the prescription
     */
    @PostMapping("/prescription/send")
    public ResponseEntity<String> sendPrescription(@RequestHeader("X-Username") String username) {
        log.trace("Entering sendPrescription method in ReportController class");
        log.info("Sending prescription for user: {}", username);
        try {
            this.reportService.sendPrescription(username);
            log.trace("Exiting sendPrescription method in ReportController class");
            return new ResponseEntity<>("Prescription sent successfully.", HttpStatus.OK);
        } catch (UnexpectedException ex) {
            log.error("Error sending prescription for user: {}", username, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
