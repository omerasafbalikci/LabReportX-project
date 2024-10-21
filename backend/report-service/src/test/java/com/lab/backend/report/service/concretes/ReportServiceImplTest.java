package com.lab.backend.report.service.concretes;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.entity.Report;
import com.lab.backend.report.repository.ReportRepository;
import com.lab.backend.report.repository.ReportSpecification;
import com.lab.backend.report.utilities.exceptions.*;
import com.lab.backend.report.utilities.mappers.ReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReportMapper reportMapper;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private PrescriptionService prescriptionService;
    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetReportById_success() {
        // Arrange
        Long reportId = 1L;
        Report report = new Report();
        GetReportResponse response = new GetReportResponse();

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));
        when(reportMapper.toGetReportResponse(report)).thenReturn(response);

        GetReportResponse result = reportService.getReportById(reportId);

        // Assert
        assertNotNull(result);
        verify(reportRepository).findByIdAndDeletedFalse(reportId);
        verify(reportMapper).toGetReportResponse(report);
    }

    @Test
    void testGetReportById_reportNotFound() {
        // Arrange
        Long reportId = 1L;

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ReportNotFoundException.class, () -> reportService.getReportById(reportId));
        verify(reportRepository).findByIdAndDeletedFalse(reportId);
    }

    @Test
    void testGetAllReportsFilteredAndSorted() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "date";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortBy);

        List<Report> reports = List.of(new Report());
        Page<Report> reportPage = new PageImpl<>(reports, pageable, reports.size());
        GetReportResponse response = new GetReportResponse();

        when(reportRepository.findAll(any(ReportSpecification.class), eq(pageable))).thenReturn(reportPage);
        when(reportMapper.toGetReportResponse(any(Report.class))).thenReturn(response);

        PagedResponse<GetReportResponse> result = reportService.getAllReportsFilteredAndSorted(page, size, sortBy, direction, null, null, null, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        verify(reportRepository).findAll(any(ReportSpecification.class), eq(pageable));
    }

    @Test
    void testGetReportsByTechnician() {
        // Arrange
        String username = "technician1";
        int page = 0;
        int size = 10;
        String sortBy = "date";
        String direction = "DESC";
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortBy);

        List<Report> reports = List.of(new Report());
        Page<Report> reportPage = new PageImpl<>(reports, pageable, reports.size());
        GetReportResponse response = new GetReportResponse();

        when(reportRepository.findAll(any(ReportSpecification.class), eq(pageable))).thenReturn(reportPage);
        when(reportMapper.toGetReportResponse(any(Report.class))).thenReturn(response);

        PagedResponse<GetReportResponse> result = reportService.getReportsByTechnician(username, page, size, sortBy, direction, null, null, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        verify(reportRepository).findAll(any(ReportSpecification.class), eq(pageable));
    }

    @Test
    void testAddReport_success() {
        // Arrange
        String username = "techUser";
        String trIdNumber = "12345678901";
        CreateReportRequest createReportRequest = new CreateReportRequest();
        Report report = new Report();
        report.setId(1L);
        report.setTechnicianUsername(username);
        GetReportResponse expectedResponse = new GetReportResponse();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("validTcForReport:" + username)).thenReturn(trIdNumber);
        when(reportMapper.toReport(createReportRequest)).thenReturn(report);
        when(reportRepository.save(report)).thenReturn(report);
        when(reportMapper.toGetReportResponse(report)).thenReturn(expectedResponse);

        // Act
        GetReportResponse actualResponse = reportService.addReport(username, createReportRequest);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(redisTemplate).delete("validTcForReport:" + username);
        verify(reportRepository).save(report);
    }

    @Test
    void testAddReport_trIdNotFound() {
        // Arrange
        String username = "techUser";
        CreateReportRequest createReportRequest = new CreateReportRequest();

        when(redisTemplate.opsForValue().get("validTcForReport:" + username)).thenReturn(null);

        // Act & Assert
        assertThrows(InvalidTrIdNumberException.class, () -> reportService.addReport(username, createReportRequest));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void testUpdateReport_success() {
        // Arrange
        String username = "tech1";
        UpdateReportRequest request = new UpdateReportRequest();
        request.setId(1L);
        request.setDiagnosisTitle("Updated Title");

        Report existingReport = new Report();
        existingReport.setId(1L);
        existingReport.setTechnicianUsername(username);
        existingReport.setDiagnosisTitle("Old Title");

        when(reportRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingReport));
        when(reportMapper.toGetReportResponse(any(Report.class))).thenReturn(new GetReportResponse());

        // Act
        GetReportResponse response = reportService.updateReport(username, request);

        // Assert
        assertNotNull(response);
        verify(reportRepository).save(existingReport);
        assertEquals("Updated Title", existingReport.getDiagnosisTitle());
    }

    @Test
    void testUpdateReport_reportNotFound() {
        // Arrange
        String username = "tech1";
        UpdateReportRequest request = new UpdateReportRequest();
        request.setId(1L);

        when(reportRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ReportNotFoundException exception = assertThrows(ReportNotFoundException.class, () -> reportService.updateReport(username, request));

        assertEquals("Report doesn't exist with id 1", exception.getMessage());
        verify(reportRepository).findByIdAndDeletedFalse(1L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testUpdateReport_unauthorizedAccess() {
        // Arrange
        String username = "tech2";
        UpdateReportRequest request = new UpdateReportRequest();
        request.setId(1L);

        Report existingReport = new Report();
        existingReport.setId(1L);
        existingReport.setTechnicianUsername("tech1");

        when(reportRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingReport));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> reportService.updateReport(username, request));
    }

    @Test
    void testDeleteReport_success() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;

        Report existingReport = new Report();
        existingReport.setId(reportId);
        existingReport.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(existingReport));

        // Act
        reportService.deleteReport(username, reportId);

        // Assert
        verify(reportRepository).save(existingReport);
        assertTrue(existingReport.isDeleted());
    }

    @Test
    void testDeleteReport_reportNotFound() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        ReportNotFoundException exception = assertThrows(ReportNotFoundException.class, () -> reportService.deleteReport(username, reportId));

        assertEquals("Report doesn't exist with id 1", exception.getMessage());
        verify(reportRepository).findByIdAndDeletedFalse(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testDeleteReport_unauthorizedAccess() {
        // Arrange
        String username = "tech2";
        Long reportId = 1L;

        Report existingReport = new Report();
        existingReport.setId(reportId);
        existingReport.setTechnicianUsername("tech1");

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(existingReport));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> reportService.deleteReport(username, reportId));
    }

    @Test
    void testRestoreReport_success() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;

        Report deletedReport = new Report();
        deletedReport.setId(reportId);
        deletedReport.setTechnicianUsername(username);
        deletedReport.setDeleted(true);

        when(reportRepository.findByIdAndDeletedTrue(reportId)).thenReturn(Optional.of(deletedReport));
        when(reportMapper.toGetReportResponse(any(Report.class))).thenReturn(new GetReportResponse());

        // Act
        GetReportResponse response = reportService.restoreReport(username, reportId);

        // Assert
        assertNotNull(response);
        verify(reportRepository).save(deletedReport);
        assertFalse(deletedReport.isDeleted());
    }

    @Test
    void testRestoreReport_reportNotFound() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;

        when(reportRepository.findByIdAndDeletedTrue(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        ReportNotFoundException exception = assertThrows(ReportNotFoundException.class, () -> reportService.restoreReport(username, reportId));

        assertEquals("Report doesn't exist with id " + reportId, exception.getMessage());
        verify(reportRepository).findByIdAndDeletedTrue(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testRestoreReport_unauthorizedAccess() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;

        Report report = new Report();
        report.setId(reportId);
        report.setTechnicianUsername("tech2");
        report.setDeleted(true);

        when(reportRepository.findByIdAndDeletedTrue(reportId)).thenReturn(Optional.of(report));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> reportService.restoreReport(username, reportId));

        assertEquals("You are not authorized to restore this report.", exception.getMessage());
        verify(reportRepository).findByIdAndDeletedTrue(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testAddPhoto_success() throws IOException {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;
        MultipartFile photo = mock(MultipartFile.class);
        String originalFileName = "photo.png";
        when(photo.getOriginalFilename()).thenReturn(originalFileName);
        when(photo.getInputStream()).thenReturn(mock(java.io.InputStream.class));

        Report report = new Report();
        report.setId(reportId);
        report.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        // Act
        reportService.addPhoto(username, reportId, photo);

        // Assert
        verify(reportRepository).save(report);
        assertNotNull(report.getPhotoPath());
        verify(photo).getInputStream();
    }

    @Test
    void testAddPhoto_reportNotFound() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;
        MultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "photo content".getBytes());

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        ReportNotFoundException exception = assertThrows(ReportNotFoundException.class, () -> reportService.addPhoto(username, reportId, photo));

        assertEquals("Report doesn't exist with id " + reportId, exception.getMessage());
        verify(reportRepository).findByIdAndDeletedFalse(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testAddPhoto_unauthorizedAccess() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;
        MultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "photo content".getBytes());

        Report report = new Report();
        report.setId(reportId);
        report.setTechnicianUsername("tech2");
        report.setPhotoPath(null);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> reportService.addPhoto(username, reportId, photo));

        assertEquals("You are not authorized to restore this report.", exception.getMessage());
        verify(reportRepository).findByIdAndDeletedFalse(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void testAddPhoto_fileAlreadyExists() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;
        MultipartFile photo = mock(MultipartFile.class);

        Report report = new Report();
        report.setId(reportId);
        report.setTechnicianUsername(username);
        report.setPhotoPath("existingPath/photo.png");

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        // Act & Assert
        assertThrows(FileStorageException.class, () -> reportService.addPhoto(username, reportId, photo));
    }

    @Test
    void testAddPhoto_invalidFileFormat() {
        // Arrange
        String username = "tech1";
        Long reportId = 1L;
        MultipartFile photo = mock(MultipartFile.class);
        when(photo.getOriginalFilename()).thenReturn("photo.txt");

        Report report = new Report();
        report.setId(reportId);
        report.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        // Act & Assert
        assertThrows(FileStorageException.class, () -> reportService.addPhoto(username, reportId, photo));
    }

    @Test
    void testGetPhoto_success() {
        // Arrange
        Long reportId = 1L;
        String username = "technicianUser";
        String photoPath = "backend/uploads/photos/photo.jpg";

        Report report = new Report();
        report.setPhotoPath(photoPath);
        report.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        // Act & Assert
        byte[] expectedBytes = "photo content".getBytes();
        Path path = Paths.get(photoPath);
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(path)).thenReturn(expectedBytes);
            byte[] result = reportService.getPhoto(username, reportId);
            assertArrayEquals(expectedBytes, result);
        }
    }

    @Test
    void testGetPhoto_reportNotFound() {
        // Arrange
        Long reportId = 1L;
        String username = "technicianUser";

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ReportNotFoundException.class, () -> reportService.getPhoto(username, reportId));

        // Act & Assert
        assertEquals("Report doesn't exist with id " + reportId, exception.getMessage());
    }

    @Test
    void testGetPhoto_unauthorizedAccess() {
        // Arrange
        Long reportId = 1L;
        String username = "differentUser";

        Report report = new Report();
        report.setTechnicianUsername("technicianUser");

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        Exception exception = assertThrows(UnauthorizedAccessException.class, () -> reportService.getPhoto(username, reportId));

        // Act & Assert
        assertEquals("You are not authorized to restore this report.", exception.getMessage());
    }

    @Test
    void testGetPhoto_noPhotoFound() {
        // Arrange
        Long reportId = 1L;
        String username = "technicianUser";

        Report report = new Report();
        report.setPhotoPath(null);
        report.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        Exception exception = assertThrows(FileStorageException.class, () -> reportService.getPhoto(username, reportId));

        // Act & Assert
        assertEquals("No photo found for report with id " + reportId, exception.getMessage());
    }

    @Test
    void testDeletePhoto_success() {
        // Arrange
        Long reportId = 1L;
        String username = "technicianUser";
        String photoPath = "backend/uploads/photos/photo.jpg";

        Report report = new Report();
        report.setPhotoPath(photoPath);
        report.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        // Act & Assert
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(Paths.get(photoPath))).thenReturn(true);
            reportService.deletePhoto(username, reportId);
            assertNull(report.getPhotoPath());
            verify(reportRepository).save(report);
        }
    }

    @Test
    void testDeletePhoto_reportNotFound() {
        // Arrange
        Long reportId = 1L;
        String username = "technicianUser";

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ReportNotFoundException.class, () -> reportService.deletePhoto(username, reportId));

        // Act & Assert
        assertEquals("Report doesn't exist with id " + reportId, exception.getMessage());
    }

    @Test
    void testDeletePhoto_UnauthorizedAccess() {
        // Arrange
        Long reportId = 1L;
        String username = "unauthorizedUser";
        String technicianUsername = "technicianUser";

        Report report = new Report();
        report.setPhotoPath("backend/uploads/photos/photo.jpg");
        report.setTechnicianUsername(technicianUsername);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        Exception exception = assertThrows(UnauthorizedAccessException.class, () -> reportService.deletePhoto(username, reportId));

        // Act & Assert
        assertEquals("You are not authorized to restore this report.", exception.getMessage());
    }

    @Test
    void testDeletePhoto_noPhotoToDelete() {
        // Arrange
        Long reportId = 1L;
        String username = "technicianUser";

        Report report = new Report();
        report.setPhotoPath(null);
        report.setTechnicianUsername(username);

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));

        Exception exception = assertThrows(FileStorageException.class, () -> reportService.deletePhoto(username, reportId));

        // Act & Assert
        assertEquals("No photo to delete for report with id " + reportId, exception.getMessage());
    }

    @Test
    void testGetReportPdf_reportNotFound() {
        // Arrange
        Long reportId = 1L;

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ReportNotFoundException.class, () -> reportService.getReportPdf(reportId));

        // Act & Assert
        assertEquals("Report not found with id: " + reportId, exception.getMessage());
    }

    @Test
    void testGetPrescription_success() {
        // Arrange
        Long reportId = 1L;
        String username = "testUser";
        byte[] pdfBytes = new byte[]{1, 2, 3};

        Report report = new Report();
        report.setDiagnosisDetails("Diagnosis Details");
        report.setPatientTrIdNumber("12345678901");

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.of(report));
        when(prescriptionService.generatePrescription(report.getDiagnosisDetails())).thenReturn(pdfBytes);

        byte[] result = reportService.getPrescription(username, reportId);

        // Act & Assert
        assertArrayEquals(pdfBytes, result);
        verify(redisTemplate).delete("prescription:" + username);
        verify(redisTemplate).delete("trIdNumber:" + username);
        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testGetPrescription_reportNotFound() {
        // Arrange
        Long reportId = 1L;
        String username = "testUser";

        when(reportRepository.findByIdAndDeletedFalse(reportId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ReportNotFoundException.class, () -> reportService.getPrescription(username, reportId));

        // Act & Assert
        assertEquals("Report doesn't exist with id " + reportId, exception.getMessage());
    }

    @Test
    void testSendPrescription_noCachedData() {
        // Arrange
        String username = "testUser";

        when(valueOperations.get("prescription:" + username)).thenReturn(null);
        when(valueOperations.get("trIdNumber:" + username)).thenReturn(null);

        Exception exception = assertThrows(UnexpectedException.class, () -> reportService.sendPrescription(username));

        // Act & Assert
        assertEquals("Prescription or TR ID number null in Redis", exception.getMessage());
    }

    @Test
    void testSendPrescription_pdfDataEmpty() {
        // Arrange
        String username = "testUser";
        String encodedTrIdNumber = "12345678901";

        when(valueOperations.get("prescription:" + username)).thenReturn(Base64.getEncoder().encodeToString(new byte[]{}));
        when(valueOperations.get("trIdNumber:" + username)).thenReturn(encodedTrIdNumber);

        Exception exception = assertThrows(UnexpectedException.class, () -> reportService.sendPrescription(username));

        // Act & Assert
        assertEquals("PDF data is empty or corrupted", exception.getMessage());
    }
}
