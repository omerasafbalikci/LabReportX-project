package com.lab.backend.report.service.concretes;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetPatientResponse;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.entity.Report;
import com.lab.backend.report.repository.ReportRepository;
import com.lab.backend.report.repository.ReportSpecification;
import com.lab.backend.report.service.abstracts.ReportService;
import com.lab.backend.report.utilities.PdfUtil;
import com.lab.backend.report.utilities.exceptions.*;
import com.lab.backend.report.utilities.mappers.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClient.Builder webClientBuilder;
    private final String jwt = HttpHeaders.AUTHORIZATION.substring(7);
    private final PdfUtil pdfUtil;
    private final DiagnosisService diagnosisService;
    private final MailService mailService;

    @Override
    public GetReportResponse getReportById(Long id) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            return new ReportNotFoundException("Report not found with id: " + id);
        });
        GetReportResponse response = this.reportMapper.toGetReportResponse(report);
        return response;
    }

    @Override
    public PagedResponse<GetReportResponse> getAllReportsFilteredAndSorted(int page, int size, String sortBy, String direction, String fileNumber,
                                                                           String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date,
                                                                           String photoPath, Boolean deleted) {
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        ReportSpecification specification = new ReportSpecification(fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, deleted);
        Page<Report> userPage = this.reportRepository.findAll(specification, pagingSort);
        List<GetReportResponse> reportResponses = userPage.getContent()
                .stream()
                .map(this.reportMapper::toGetReportResponse)
                .toList();

        return new PagedResponse<>(
                reportResponses,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userPage.isFirst(),
                userPage.isLast(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    @Override
    public PagedResponse<GetReportResponse> getReportsByTechnician(String username, int page, int size, String sortBy, String direction, String fileNumber,
                                                                   String patientTrIdNumber, String diagnosisTitle, String diagnosisDetails, String date,
                                                                   String photoPath, Boolean deleted) {
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        ReportSpecification specification = new ReportSpecification(username, fileNumber, patientTrIdNumber, diagnosisTitle, diagnosisDetails, date, photoPath, deleted);
        Page<Report> userPage = this.reportRepository.findAll(specification, pagingSort);
        List<GetReportResponse> reportResponses = userPage.getContent()
                .stream()
                .map(this.reportMapper::toGetReportResponse)
                .toList();

        return new PagedResponse<>(
                reportResponses,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userPage.isFirst(),
                userPage.isLast(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    @Override
    public String checkTrIdNumber(String username, String trIdNumber) {
        String trIdRegex = "^[1-9][0-9]{10}$";
        Pattern pattern = Pattern.compile(trIdRegex);
        Matcher matcher = pattern.matcher(trIdNumber);

        if (!matcher.matches()) {
            throw new InvalidTcException("Invalid TR ID number format");
        }

        Boolean check = null;
        try {
            check = this.webClientBuilder.build().get()
                    .uri("http://patient-service/patients/check-tc", uriBuilder ->
                            uriBuilder.queryParam("trIdNumber", trIdNumber).build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception exception) {
            throw new UnexpectedException("Error occurred while checking TR ID number: " + exception.getMessage());
        }

        if (check != null && check) {
            this.redisTemplate.delete("validTcForReport:" + username);
            this.redisTemplate.opsForValue().set("validTcForReport:" + username, trIdNumber, 1, TimeUnit.HOURS);
            return "TR ID number is valid. Redirecting to report creation...";
        } else {
            return "TR ID number is invalid or not found.";
        }
    }

    @Override
    public GetReportResponse addReport(String username, CreateReportRequest createReportRequest) {
        String trIdNumber = this.redisTemplate.opsForValue().get("validTcForReport:" + username);
        if (trIdNumber != null) {
            Report report = this.reportMapper.toReport(createReportRequest);
            report.setPatientTrIdNumber(trIdNumber);
            report.setTechnicianUsername(username);
            this.reportRepository.save(report);
            this.redisTemplate.delete("validTcForReport:" + username);
            GetReportResponse response = this.reportMapper.toGetReportResponse(report);
            return response;
        } else {
            throw new InvalidTcException("TR ID number not found in Redis, please validate first.");
        }
    }

    @Override
    public GetReportResponse updateReport(UpdateReportRequest updateReportRequest) {
        Report existingReport = this.reportRepository.findByIdAndDeletedFalse(updateReportRequest.getId())
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + updateReportRequest.getId());
                });

        if (updateReportRequest.getDiagnosisTitle() != null && !existingReport.getDiagnosisTitle().equals(updateReportRequest.getDiagnosisTitle())) {
            existingReport.setDiagnosisTitle(updateReportRequest.getDiagnosisTitle());
        }
        if (updateReportRequest.getDiagnosisDetails() != null && !existingReport.getDiagnosisDetails().equals(updateReportRequest.getDiagnosisDetails())) {
            existingReport.setDiagnosisDetails(updateReportRequest.getDiagnosisDetails());
        }
        existingReport.setDate(LocalDateTime.now());
        this.reportRepository.save(existingReport);
        GetReportResponse response = this.reportMapper.toGetReportResponse(existingReport);
        return response;
    }

    @Override
    public void deleteReport(Long id) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + id);
                });
        report.setDeleted(true);
        this.reportRepository.save(report);
    }

    @Override
    public GetReportResponse restoreReport(Long id) {
        Report report = this.reportRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + id);
                });
        report.setDeleted(false);
        this.reportRepository.save(report);
        GetReportResponse response = this.reportMapper.toGetReportResponse(report);
        return response;
    }

    @Override
    public void addPhoto(Long reportId, MultipartFile photo) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(reportId)
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + reportId);
                });

        String uploadDir = "uploads/photos/";
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(photo.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = UUID.randomUUID() + "." + fileExtension;

        try {
            Path uploadPath = Paths.get(uploadDir + newFileName);
            Files.createDirectories(uploadPath.getParent());
            Files.copy(photo.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            report.setPhotoPath(uploadPath.toString());
            this.reportRepository.save(report);
        } catch (IOException exception) {
            throw new FileStorageException("Could not store file " + newFileName + ". Please try again! " + exception);
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    @Override
    public byte[] getPhoto(Long reportId) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(reportId)
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + reportId);
                });
        String photoPath = report.getPhotoPath();
        if (photoPath == null || photoPath.isEmpty()) {
            throw new FileStorageException("No photo found for report with id " + reportId);
        }

        try {
            Path filePath = Paths.get(photoPath);
            return Files.readAllBytes(filePath);
        } catch (IOException exception) {
            throw new FileStorageException("Could not read file: " + photoPath + ". Please try again! " + exception);
        }
    }

    @Override
    public void deletePhoto(Long reportId) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(reportId)
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + reportId);
                });
        String photoPath = report.getPhotoPath();
        if (photoPath == null || photoPath.isEmpty()) {
            throw new FileStorageException("No photo to delete for report with id " + reportId);
        }

        try {
            Path filePath = Paths.get(photoPath);
            Files.deleteIfExists(filePath);
            report.setPhotoPath(null);
            this.reportRepository.save(report);
        } catch (IOException exception) {
            throw new FileStorageException("Could not delete file: " + photoPath + ". Please try again! " + exception);
        }
    }

    private Mono<GetPatientResponse> getPatientByTc(String jwt, String trIdNumber) {
        String trIdRegex = "^[1-9][0-9]{10}$";
        Pattern pattern = Pattern.compile(trIdRegex);
        Matcher matcher = pattern.matcher(trIdNumber);

        if (!matcher.matches()) {
            throw new InvalidTcException("Invalid TR ID number format");
        }

        return this.webClientBuilder.build().get()
                .uri("http://patient-service/patients/tr-id-number", uriBuilder ->
                        uriBuilder.queryParam("trIdNumber", trIdNumber).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new PatientNotFoundException("Client error: " + errorBody))))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new UnexpectedException("Server error: " + errorBody))))
                .bodyToMono(GetPatientResponse.class);
    }

    @Override
    public Mono<byte[]> getReportPdf(Long reportId) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(reportId).orElseThrow(() -> {
            return new ReportNotFoundException("Report not found with id: " + reportId);
        });
        GetReportResponse reportResponse = this.reportMapper.toGetReportResponse(report);
        return getPatientByTc(jwt, reportResponse.getPatientTrIdNumber())
                .flatMap(patientResponse -> {
                    byte[] pdfBytes = this.pdfUtil.generatePdf(reportResponse, patientResponse);
                    return Mono.just(pdfBytes);
                });
    }

    @Override
    public byte[] getPrescription(String username, Long reportId) {
        Report report = this.reportRepository.findByIdAndDeletedFalse(reportId)
                .orElseThrow(() -> {
                    return new ReportNotFoundException("Report doesn't exist with id " + reportId);
                });
        byte[] pdfBytes = this.diagnosisService.generatePrescription(report.getDiagnosisDetails());
        this.redisTemplate.delete("prescription:" + username);
        this.redisTemplate.delete("tc:" + username);
        this.redisTemplate.opsForValue().set("prescription:" + username, Base64.getEncoder().encodeToString(pdfBytes), 1, TimeUnit.HOURS);
        this.redisTemplate.opsForValue().set("tc:" + username, report.getPatientTrIdNumber(), 1, TimeUnit.HOURS);
        return pdfBytes;
    }

    @Override
    public void sendPrescription(String username) {
        String encodedPrescription = this.redisTemplate.opsForValue().get("prescription:" + username);
        String encodedTrIdNumber = this.redisTemplate.opsForValue().get("tc:" + username);
        if (encodedPrescription != null && encodedTrIdNumber != null) {
            byte[] pdfBytes = Base64.getDecoder().decode(encodedPrescription);
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new UnexpectedException("PDF data is empty or corrupted");
            }
            String email = getEmail(jwt, encodedTrIdNumber);
            this.mailService.sendEmail(email, "Your Prescription", "Here is your prescription.", pdfBytes, "prescription.pdf");
            this.redisTemplate.delete("prescription:" + username);
            this.redisTemplate.delete("tc:" + username);
        } else {
            throw new UnexpectedException("Prescription or TC null in Redis");
        }
    }

    private String getEmail(String jwt, String trIdNumber) {
        String email;
        try {
            email = this.webClientBuilder.build().get()
                    .uri("http://patient-service/patients/email", uriBuilder ->
                            uriBuilder.queryParam("trIdNumber", trIdNumber).build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new PatientNotFoundException("Patient not found in patient service with TC: " + trIdNumber);
        } catch (Exception e) {
            throw new UnexpectedException("Error occurred while calling patient service: " + e);
        }
        if (email != null) {
            return email;
        } else {
            throw new EmailNullException("Email is null or empty");
        }
    }
}
