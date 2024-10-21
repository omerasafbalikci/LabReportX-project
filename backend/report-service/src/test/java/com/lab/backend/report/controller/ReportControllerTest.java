package com.lab.backend.report.controller;

import com.lab.backend.report.dto.requests.CreateReportRequest;
import com.lab.backend.report.dto.requests.UpdateReportRequest;
import com.lab.backend.report.dto.responses.GetReportResponse;
import com.lab.backend.report.dto.responses.PagedResponse;
import com.lab.backend.report.service.abstracts.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {
    @Mock
    private ReportService reportService;
    @InjectMocks
    private ReportController reportController;

    @Test
    public void testGetReportById_success() {
        // Arrange
        Long reportId = 1L;
        GetReportResponse expectedResponse = new GetReportResponse();

        when(reportService.getReportById(reportId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetReportResponse> response = reportController.getReportById(reportId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        Mockito.verify(reportService).getReportById(reportId);
    }

    @Test
    public void testGetAllReportsFilteredAndSorted_success() {
        // Arrange
        PagedResponse<GetReportResponse> expectedResponse = new PagedResponse<>();

        when(reportService.getAllReportsFilteredAndSorted(0, 3, "id", "ASC", null, null, null, null, null, null, null, null))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagedResponse<GetReportResponse>> response = reportController.getAllReportsFilteredAndSorted(0, 3, "id", "ASC", null, null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        Mockito.verify(reportService).getAllReportsFilteredAndSorted(0, 3, "id", "ASC", null, null, null, null, null, null, null, null);
    }

    @Test
    public void testGetAllReportsByTechnician_success() {
        // Arrange
        String username = "technician1";
        PagedResponse<GetReportResponse> expectedResponse = new PagedResponse<>();

        when(reportService.getReportsByTechnician(username, 0, 3, "id", "ASC", null, null, null, null, null, null, null))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagedResponse<GetReportResponse>> response = reportController.getAllReportsByTechnician(username, 0, 3, "id", "ASC", null, null, null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        Mockito.verify(reportService).getReportsByTechnician(username, 0, 3, "id", "ASC", null, null, null, null, null, null, null);
    }

    @Test
    public void testCheckTrIdNumber_success() {
        // Arrange
        String username = "technician1";
        String trIdNumber = "12345678901";
        String expectedMessage = "TR ID is valid";

        when(reportService.checkTrIdNumber(username, trIdNumber)).thenReturn(expectedMessage);

        // Act
        ResponseEntity<String> response = reportController.checkTrIdNumber(username, trIdNumber);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedMessage, response.getBody());
        Mockito.verify(reportService).checkTrIdNumber(username, trIdNumber);
    }

    @Test
    public void testAddReport_success() {
        // Arrange
        String username = "technician1";
        CreateReportRequest createReportRequest = new CreateReportRequest();
        GetReportResponse expectedResponse = new GetReportResponse();

        when(reportService.addReport(username, createReportRequest)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetReportResponse> response = reportController.addReport(username, createReportRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        Mockito.verify(reportService).addReport(username, createReportRequest);
    }

    @Test
    public void updateReport_shouldReturnUpdatedReport_whenValidRequest() {
        // Arrange
        String username = "technician1";
        UpdateReportRequest updateReportRequest = new UpdateReportRequest();
        GetReportResponse response = new GetReportResponse();
        when(reportService.updateReport(username, updateReportRequest)).thenReturn(response);

        // Act
        ResponseEntity<GetReportResponse> result = reportController.updateReport(username, updateReportRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    public void deleteReport_shouldReturnSuccessMessage_whenValidId() {
        // Arrange
        String username = "technician1";
        Long reportId = 1L;

        // Act
        ResponseEntity<String> result = reportController.deleteReport(username, reportId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Report has been successfully deleted.", result.getBody());
    }

    @Test
    public void restoreReport_shouldReturnRestoredReport_whenValidId() {
        // Arrange
        String username = "technician1";
        Long reportId = 1L;
        GetReportResponse response = new GetReportResponse();
        when(reportService.restoreReport(username, reportId)).thenReturn(response);

        // Act
        ResponseEntity<GetReportResponse> result = reportController.restoreReport(username, reportId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    public void addPhoto_shouldReturnSuccessMessage_whenPhotoUploaded() {
        // Arrange
        String username = "technician1";
        Long reportId = 1L;
        MultipartFile photo = new MockMultipartFile("photo", "filename.jpg", "image/jpeg", "test".getBytes());

        // Act
        ResponseEntity<String> result = reportController.addPhoto(username, reportId, photo);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Photo uploaded successfully.", result.getBody());
    }

    @Test
    public void getPhoto_shouldReturnPhotoData_whenValidId() {
        // Arrange
        String username = "technician1";
        Long reportId = 1L;
        byte[] photoData = "test".getBytes();
        when(reportService.getPhoto(username, reportId)).thenReturn(photoData);

        // Act
        ResponseEntity<byte[]> result = reportController.getPhoto(username, reportId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertArrayEquals(photoData, result.getBody());
        assertEquals(MediaType.IMAGE_JPEG, result.getHeaders().getContentType());
    }

    @Test
    public void deletePhoto_shouldReturnSuccessMessage_whenValidId() {
        // Arrange
        String username = "technician1";
        Long reportId = 1L;

        // Act
        ResponseEntity<String> result = reportController.deletePhoto(username, reportId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Photo deleted successfully.", result.getBody());
    }

    @Test
    public void getReportPdf_shouldReturnPdfBytes_whenValidId() {
        // Arrange
        Long reportId = 1L;
        byte[] pdfBytes = "pdf data".getBytes();
        when(reportService.getReportPdf(reportId)).thenReturn(Mono.just(pdfBytes));

        // Act
        ResponseEntity<Mono<byte[]>> result = reportController.getReportPdf(reportId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
    }

    @Test
    public void getPrescription_shouldReturnPrescriptionData_whenValidId() {
        // Arrange
        String username = "technician1";
        Long reportId = 1L;
        byte[] pdfBytes = "pdf data".getBytes();
        when(reportService.getPrescription(username, reportId)).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> result = reportController.getPrescription(username, reportId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertArrayEquals(pdfBytes, result.getBody());
        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
    }

    @Test
    public void sendPrescription_shouldReturnSuccessMessage_whenSent() {
        // Arrange
        String username = "technician1";

        // Act
        ResponseEntity<String> result = reportController.sendPrescription(username);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Prescription sent successfully.", result.getBody());
    }
}
