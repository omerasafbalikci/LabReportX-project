package com.lab.backend.patient.controller;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.service.abstracts.PatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {
    @Mock
    private PatientService patientService;
    @InjectMocks
    private PatientController patientController;

    @Test
    public void testGetPatientById() {
        // Arrange
        Long patientId = 1L;
        GetPatientResponse mockResponse = new GetPatientResponse();
        when(patientService.getPatientById(patientId)).thenReturn(mockResponse);

        ResponseEntity<GetPatientResponse> responseEntity = patientController.getPatientById(patientId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

    @Test
    public void testGetPatientByTrIdNumber() {
        // Arrange
        String trIdNumber = "12345678901";
        GetPatientResponse mockResponse = new GetPatientResponse();
        when(patientService.getPatientByTrIdNumber(trIdNumber)).thenReturn(mockResponse);

        ResponseEntity<GetPatientResponse> responseEntity = patientController.getPatientByTrIdNumber(trIdNumber);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

    @Test
    public void testGetChronicDiseasesById() {
        // Arrange
        Long patientId = 1L;
        Set<String> mockDiseases = new HashSet<>(List.of("Disease1", "Disease2"));
        when(patientService.getChronicDiseasesById(patientId)).thenReturn(mockDiseases);

        ResponseEntity<Set<String>> responseEntity = patientController.getChronicDiseasesById(patientId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockDiseases, responseEntity.getBody());
    }

    @Test
    public void testGetEmail() {
        // Arrange
        String trIdNumber = "12345678901";
        String mockEmail = "test@example.com";
        when(patientService.getEmail(trIdNumber)).thenReturn(mockEmail);

        ResponseEntity<String> responseEntity = patientController.getEmail(trIdNumber);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockEmail, responseEntity.getBody());
    }

    @Test
    public void testIsPatientRegistered() {
        // Arrange
        String trIdNumber = "12345678901";
        when(patientService.isPatientRegistered(trIdNumber)).thenReturn(true);

        ResponseEntity<Boolean> responseEntity = patientController.isPatientRegistered(trIdNumber);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(true, responseEntity.getBody());
    }

    @Test
    public void testGetAllPatientsFilteredAndSorted() {
        // Arrange
        GetPatientResponse patientResponse = new GetPatientResponse(
                1L, "John", "Doe", "12345678901", "1980-01-01",
                "MALE", "A_POSITIVE", "+905458654785", "john.doe@example.com",
                "2024-01-01");
        PagedResponse<GetPatientResponse> pagedResponse = new PagedResponse<>(
                Collections.singletonList(patientResponse), 1, 1, 1, 1, true, true, false, false);

        when(patientService.getAllPatientsFilteredAndSorted(
                anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<GetPatientResponse>> response = patientController.getAllPatientsFilteredAndSorted(
                0, 3, "id", "ASC", "John", "Doe", "12345678901", "1980-01-01",
                "Male", "A+", "1234567890", "john.doe@example.com",
                "None", "2024-01-01", false);

        // Assert
        assertEquals(1, Objects.requireNonNull(response.getBody()).getContent().size());
        assertEquals("John", response.getBody().getContent().get(0).getFirstName());
        assertEquals("Doe", response.getBody().getContent().get(0).getLastName());
        assertEquals("12345678901", response.getBody().getContent().get(0).getTrIdNumber());
        assertEquals("1980-01-01", response.getBody().getContent().get(0).getBirthDate());
    }

    @Test
    public void testSavePatient() {
        // Arrange
        CreatePatientRequest request = new CreatePatientRequest();
        GetPatientResponse mockResponse = new GetPatientResponse();
        when(patientService.savePatient(request)).thenReturn(mockResponse);

        ResponseEntity<GetPatientResponse> responseEntity = patientController.savePatient(request);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

    @Test
    public void testUpdatePatient() {
        // Arrange
        UpdatePatientRequest request = new UpdatePatientRequest();
        GetPatientResponse mockResponse = new GetPatientResponse();
        when(patientService.updatePatient(request)).thenReturn(mockResponse);

        ResponseEntity<GetPatientResponse> responseEntity = patientController.updatePatient(request);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

    @Test
    public void testDeletePatient() {
        // Arrange
        Long patientId = 1L;
        ResponseEntity<String> responseEntity = patientController.deletePatient(patientId);

        // Assert
        Mockito.verify(patientService).deletePatient(patientId);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Patient has been successfully deleted.", responseEntity.getBody());
    }

    @Test
    public void testRestorePatient() {
        // Arrange
        Long patientId = 1L;
        GetPatientResponse mockResponse = new GetPatientResponse();
        when(patientService.restorePatient(patientId)).thenReturn(mockResponse);

        ResponseEntity<GetPatientResponse> responseEntity = patientController.restorePatient(patientId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }
}
