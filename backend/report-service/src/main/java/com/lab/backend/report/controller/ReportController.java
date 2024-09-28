package com.lab.backend.report.controller;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.service.abstracts.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Log4j2
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/id/{id}")
    public ResponseEntity<GetReportResponse> getReportById(@PathVariable Long id) {
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

    @PostMapping
    public ResponseEntity<GetReportResponse> addReport(@RequestHeader("X-Username") String username, @RequestBody @Valid CreateReportRequest createReportRequest) {
        GetReportResponse response = this.reportService.addReport(username, createReportRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<GetReportResponse> addReport(@RequestBody @Valid UpdateReportRequest updateReportRequest) {
        GetReportResponse response = this.reportService.updateReport(updateReportRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(@PathVariable Long id) {
        this.reportService.deleteReport(id);
        return ResponseEntity.ok("Report has been successfully deleted.");
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<GetReportResponse> restoreReport(@PathVariable Long id) {
        GetReportResponse response = this.reportService.restoreReport(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/photo/{reportId}")
    public ResponseEntity<String> addPhoto(@PathVariable Long reportId, @RequestParam("photo") MultipartFile photo) {
        this.reportService.addPhoto(reportId, photo);
        return ResponseEntity.ok("Photo uploaded successfully.");
    }

    @GetMapping("/photo/{reportId}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long reportId) {
        byte[] photoData = this.reportService.getPhoto(reportId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photoData);
    }

    @DeleteMapping("/photo/{reportId}")
    public ResponseEntity<String> deletePhoto(@PathVariable Long reportId) {
        this.reportService.deletePhoto(reportId);
        return ResponseEntity.ok("Photo deleted successfully.");
    }
}
