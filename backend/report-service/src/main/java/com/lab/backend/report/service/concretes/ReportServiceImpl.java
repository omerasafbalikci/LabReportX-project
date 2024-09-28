package com.lab.backend.report.service.concretes;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.entity.Report;
import com.lab.backend.report.repository.ReportRepository;
import com.lab.backend.report.repository.ReportSpecification;
import com.lab.backend.report.service.abstracts.ReportService;
import com.lab.backend.report.utilities.exceptions.FileStorageException;
import com.lab.backend.report.utilities.exceptions.ReportNotFoundException;
import com.lab.backend.report.utilities.mappers.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReportServiceImpl implements ReportService {
    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routingKey}")
    private String ROUTING_KEY_CREATE;

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final RabbitTemplate rabbitTemplate;

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
    public GetReportResponse addReport(String username, CreateReportRequest createReportRequest) {
        Report report = this.reportMapper.toReport(createReportRequest);
        report.setTechnicianUsername(username);
        this.reportRepository.save(report);
        GetReportResponse response = this.reportMapper.toGetReportResponse(report);
        return response;
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
}
