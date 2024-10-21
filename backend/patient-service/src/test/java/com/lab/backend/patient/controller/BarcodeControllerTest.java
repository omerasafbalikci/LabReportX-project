package com.lab.backend.patient.controller;

import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.service.abstracts.BarcodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BarcodeControllerTest {
    @Mock
    private BarcodeService barcodeService;
    @InjectMocks
    private BarcodeController barcodeController;

    @Test
    void scanAndSavePatient_shouldReturnPatientResponse() {
        // Arrange
        GetPatientResponse expectedResponse = new GetPatientResponse(); // Initialize with required fields
        when(barcodeService.scanAndSavePatient()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<GetPatientResponse> response = barcodeController.scanAndSavePatient();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(barcodeService, times(1)).scanAndSavePatient();
    }

    @Test
    void generateBarcodeForPatient_shouldReturnBarcodeImage() {
        // Arrange
        String trIdNumber = "12345678901";
        byte[] expectedBarcode = new byte[]{1, 2, 3};
        when(barcodeService.generateBarcodeForPatient(trIdNumber)).thenReturn(expectedBarcode);

        // Act
        ResponseEntity<byte[]> response = barcodeController.generateBarcodeForPatient(trIdNumber);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(expectedBarcode, response.getBody());
        assertEquals("image/png", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals(expectedBarcode.length, response.getHeaders().getContentLength());
        verify(barcodeService, times(1)).generateBarcodeForPatient(trIdNumber);
    }
}
