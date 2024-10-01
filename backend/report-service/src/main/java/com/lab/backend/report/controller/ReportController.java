package com.lab.backend.report.controller;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.service.abstracts.ReportService;
import com.lab.backend.report.utilities.exceptions.InvalidTcException;
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

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Log4j2
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/id/{id}")
    public ResponseEntity<GetReportResponse> getReportById(@PathVariable("id") Long id) {
        GetReportResponse response = this.reportService.getReportById(id);
        return ResponseEntity.ok(response);
    }

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
            @RequestParam(required = false) Boolean deleted
    ) {
        PagedResponse<GetReportResponse> response = this.reportService.getAllReportsFilteredAndSorted(page, size, sortBy, direction, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, deleted);
        return ResponseEntity.ok(response);
    }

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
        PagedResponse<GetReportResponse> response = this.reportService.getReportsByTechnician(username, page, size, sortBy, direction, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, deleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-tr-id")
    public ResponseEntity<String> checkTrIdNumber(@RequestHeader("X-Username") String username, @RequestParam String trIdNumber) {
        String responseMessage;
        try {
            responseMessage = this.reportService.checkTrIdNumber(username, trIdNumber);
            return ResponseEntity.ok(responseMessage);
        } catch (InvalidTcException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UnexpectedException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<GetReportResponse> addReport(@RequestHeader("X-Username") String username, @RequestBody @Valid CreateReportRequest createReportRequest) {
        GetReportResponse response = this.reportService.addReport(username, createReportRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<GetReportResponse> updateReport(@RequestBody @Valid UpdateReportRequest updateReportRequest) {
        GetReportResponse response = this.reportService.updateReport(updateReportRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(@PathVariable("id") Long id) {
        this.reportService.deleteReport(id);
        return ResponseEntity.ok("Report has been successfully deleted.");
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<GetReportResponse> restoreReport(@PathVariable("id") Long id) {
        GetReportResponse response = this.reportService.restoreReport(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/photo/{reportId}")
    public ResponseEntity<String> addPhoto(@PathVariable("reportId") Long reportId, @RequestParam("photo") MultipartFile photo) {
        this.reportService.addPhoto(reportId, photo);
        return ResponseEntity.ok("Photo uploaded successfully.");
    }

    @GetMapping("/photo/{reportId}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable("reportId") Long reportId) {
        byte[] photoData = this.reportService.getPhoto(reportId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photoData);
    }

    @DeleteMapping("/photo/{reportId}")
    public ResponseEntity<String> deletePhoto(@PathVariable("reportId") Long reportId) {
        this.reportService.deletePhoto(reportId);
        return ResponseEntity.ok("Photo deleted successfully.");
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<Mono<byte[]>> getReportPdf(@PathVariable("reportId") Long reportId) {
        Mono<byte[]> pdfBytes = this.reportService.getReportPdf(reportId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "report" + reportId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/prescription/{reportId}")
    public ResponseEntity<byte[]> getPrescription(@RequestHeader("X-Username") String username, @PathVariable("reportId") Long reportId) {
        byte[] pdfBytes = this.reportService.getPrescription(username, reportId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "patient" + "-prescription_" + dateFormat.format(new Date()) + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/prescription/send")
    public ResponseEntity<String> sendPrescription(@RequestHeader("X-Username") String username) {
        try {
            this.reportService.sendPrescription(username);
            return new ResponseEntity<>("Prescription sent successfully.", HttpStatus.OK);
        } catch (UnexpectedException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
