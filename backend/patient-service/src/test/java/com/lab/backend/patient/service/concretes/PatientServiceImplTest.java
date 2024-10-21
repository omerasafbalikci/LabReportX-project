package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.repository.PatientRepository;
import com.lab.backend.patient.utilities.exceptions.PatientAlreadyExistsException;
import com.lab.backend.patient.utilities.exceptions.PatientNotFoundException;
import com.lab.backend.patient.utilities.mappers.PatientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceImplTest {
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientMapper patientMapper;
    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    public void testGetPatientById_success() {
        // Arrange
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        GetPatientResponse response = new GetPatientResponse();

        Mockito.when(patientRepository.findByIdAndDeletedFalse(patientId)).thenReturn(Optional.of(patient));
        Mockito.when(patientMapper.toGetPatientResponse(patient)).thenReturn(response);

        GetPatientResponse result = patientService.getPatientById(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        Mockito.verify(patientRepository).findByIdAndDeletedFalse(patientId);
        Mockito.verify(patientMapper).toGetPatientResponse(patient);
    }

    @Test
    public void testGetPatientById_patientNotFound() {
        // Arrange
        Long patientId = 1L;

        Mockito.when(patientRepository.findByIdAndDeletedFalse(patientId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.getPatientById(patientId));
        Mockito.verify(patientRepository).findByIdAndDeletedFalse(patientId);
        Mockito.verifyNoInteractions(patientMapper);
    }

    @Test
    public void testGetPatientByTrIdNumber_success() {
        // Arrange
        String trIdNumber = "12345678901";
        Patient patient = new Patient();
        patient.setTrIdNumber(trIdNumber);
        GetPatientResponse response = new GetPatientResponse();

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.of(patient));
        Mockito.when(patientMapper.toGetPatientResponse(patient)).thenReturn(response);

        GetPatientResponse result = patientService.getPatientByTrIdNumber(trIdNumber);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
        Mockito.verify(patientMapper).toGetPatientResponse(patient);
    }

    @Test
    public void testGetPatientByTrIdNumber_patientNotFound() {
        // Arrange
        String trIdNumber = "12345678901";

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.getPatientByTrIdNumber(trIdNumber));
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
        Mockito.verifyNoInteractions(patientMapper);
    }

    @Test
    public void testGetChronicDiseasesById_success() {
        // Arrange
        Long patientId = 1L;
        Set<String> chronicDiseases = new HashSet<>(Arrays.asList("Diabetes", "Hypertension"));

        Mockito.when(patientRepository.findChronicDiseasesByIdAndDeletedFalse(patientId)).thenReturn(chronicDiseases);

        Set<String> result = patientService.getChronicDiseasesById(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(chronicDiseases, result);
        Mockito.verify(patientRepository).findChronicDiseasesByIdAndDeletedFalse(patientId);
    }

    @Test
    public void testGetChronicDiseasesById_noDiseases() {
        // Arrange
        Long patientId = 1L;

        Mockito.when(patientRepository.findChronicDiseasesByIdAndDeletedFalse(patientId)).thenReturn(null);

        Set<String> result = patientService.getChronicDiseasesById(patientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        Mockito.verify(patientRepository).findChronicDiseasesByIdAndDeletedFalse(patientId);
    }

    @Test
    public void testGetEmail_success() {
        // Arrange
        String trIdNumber = "12345678901";
        Patient patient = new Patient();
        patient.setTrIdNumber(trIdNumber);
        patient.setEmail("test@example.com");

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.of(patient));

        String email = patientService.getEmail(trIdNumber);

        // Assert
        assertNotNull(email);
        assertEquals("test@example.com", email);
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
    }

    @Test
    public void testGetEmail_patientNotFound() {
        // Arrange
        String trIdNumber = "12345678901";

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.getEmail(trIdNumber));
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
    }

    @Test
    public void testIsPatientRegistered_recentlyRegistered() {
        // Arrange
        String trIdNumber = "12345678901";
        LocalDateTime lastRegistrationTime = LocalDateTime.now().minusHours(3);
        Patient patient = new Patient();
        patient.setTrIdNumber(trIdNumber);
        patient.setLastPatientRegistrationTime(lastRegistrationTime);

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.of(patient));

        Boolean isRegisteredRecently = patientService.isPatientRegistered(trIdNumber);

        // Assert
        assertTrue(isRegisteredRecently);
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
    }

    @Test
    public void testIsPatientRegistered_notRecentlyRegistered() {
        // Arrange
        String trIdNumber = "12345678901";
        LocalDateTime lastRegistrationTime = LocalDateTime.now().minusHours(7);
        Patient patient = new Patient();
        patient.setTrIdNumber(trIdNumber);
        patient.setLastPatientRegistrationTime(lastRegistrationTime);

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.of(patient));

        Boolean isRegisteredRecently = patientService.isPatientRegistered(trIdNumber);

        // Assert
        assertFalse(isRegisteredRecently);
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
    }

    @Test
    public void testIsPatientRegistered_patientNotFound() {
        // Arrange
        String trIdNumber = "12345678901";

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber)).thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.isPatientRegistered(trIdNumber));
        Mockito.verify(patientRepository).findByTrIdNumberAndDeletedFalse(trIdNumber);
        Mockito.verifyNoMoreInteractions(patientRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetAllPatientsFilteredAndSorted_success() {
        // Arrange
        int page = 0, size = 10;
        String sortBy = "firstName";
        String direction = "ASC";
        String firstName = "John";

        Patient patient = new Patient();
        List<Patient> patients = Collections.singletonList(patient);
        Page<Patient> patientPage = new PageImpl<>(patients);

        Mockito.when(patientRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(patientPage);
        Mockito.when(patientMapper.toGetPatientResponse(patient))
                .thenReturn(new GetPatientResponse());

        PagedResponse<GetPatientResponse> response = patientService.getAllPatientsFilteredAndSorted(
                page, size, sortBy, direction, firstName, null, null, null, null, null,
                null, null, null, null, false
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalItems());
        Mockito.verify(patientRepository, Mockito.times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
    }

    @Test
    public void testSavePatient_newPatient() {
        // Arrange
        CreatePatientRequest request = new CreatePatientRequest();
        request.setTrIdNumber("12345678901");
        Patient patient = new Patient();

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(request.getTrIdNumber()))
                .thenReturn(Optional.empty());
        Mockito.when(patientMapper.toPatient(request))
                .thenReturn(patient);
        Mockito.when(patientRepository.save(patient))
                .thenReturn(patient);
        Mockito.when(patientMapper.toGetPatientResponse(patient))
                .thenReturn(new GetPatientResponse());

        GetPatientResponse response = patientService.savePatient(request);

        // Assert
        assertNotNull(response);
        Mockito.verify(patientRepository).save(patient);
    }

    @Test
    public void testSavePatient_updateExisting() {
        // Arrange
        CreatePatientRequest request = new CreatePatientRequest();
        request.setTrIdNumber("12345678901");
        Patient existingPatient = new Patient();
        existingPatient.setTrIdNumber(request.getTrIdNumber());

        Mockito.when(patientRepository.findByTrIdNumberAndDeletedFalse(request.getTrIdNumber()))
                .thenReturn(Optional.of(existingPatient));
        Mockito.when(patientRepository.save(existingPatient))
                .thenReturn(existingPatient);
        Mockito.when(patientMapper.toGetPatientResponse(existingPatient))
                .thenReturn(new GetPatientResponse());

        GetPatientResponse response = patientService.savePatient(request);

        // Assert
        assertNotNull(response);
        Mockito.verify(patientRepository).save(existingPatient);
    }

    @Test
    public void testUpdatePatient_success() {
        // Arrange
        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setId(1L);
        request.setTrIdNumber("98765432101");

        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setTrIdNumber("12345678901");

        Mockito.when(patientRepository.findByIdAndDeletedFalse(request.getId()))
                .thenReturn(Optional.of(existingPatient));
        Mockito.when(patientRepository.save(existingPatient))
                .thenReturn(existingPatient);
        Mockito.when(patientMapper.toGetPatientResponse(existingPatient))
                .thenReturn(new GetPatientResponse());

        GetPatientResponse response = patientService.updatePatient(request);

        // Assert
        assertNotNull(response);
        Mockito.verify(patientRepository).save(existingPatient);
    }

    @Test
    public void testUpdatePatient_patientNotFound() {
        // Arrange
        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setId(1L);

        Mockito.when(patientRepository.findByIdAndDeletedFalse(request.getId()))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.updatePatient(request));
    }

    @Test
    public void testUpdatePatient_patientAlreadyExists() {
        // Arrange
        UpdatePatientRequest request = new UpdatePatientRequest();
        request.setId(1L);
        request.setTrIdNumber("98765432101");

        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setTrIdNumber("12345678901");

        Mockito.when(patientRepository.findByIdAndDeletedFalse(request.getId()))
                .thenReturn(Optional.of(existingPatient));

        Mockito.when(patientRepository.existsByTrIdNumberAndDeletedIsFalse(request.getTrIdNumber()))
                .thenReturn(true);

        // Assert
        assertThrows(PatientAlreadyExistsException.class, () -> patientService.updatePatient(request));
    }

    @Test
    public void testDeletePatient_success() {
        // Arrange
        Long id = 1L;
        Patient patient = new Patient();
        patient.setId(id);

        Mockito.when(patientRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(patient));
        Mockito.when(patientRepository.save(patient))
                .thenReturn(patient);

        patientService.deletePatient(id);

        // Assert
        assertTrue(patient.isDeleted());
        Mockito.verify(patientRepository).save(patient);
    }

    @Test
    public void testDeletePatient_patientNotFound() {
        // Arrange
        Long id = 1L;

        Mockito.when(patientRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.deletePatient(id));
    }

    @Test
    public void testRestorePatient_success() {
        // Arrange
        Long id = 1L;
        Patient patient = new Patient();
        patient.setId(id);
        patient.setDeleted(true);

        Mockito.when(patientRepository.findByIdAndDeletedTrue(id))
                .thenReturn(Optional.of(patient));
        Mockito.when(patientRepository.save(patient))
                .thenReturn(patient);
        Mockito.when(patientMapper.toGetPatientResponse(patient))
                .thenReturn(new GetPatientResponse());

        GetPatientResponse response = patientService.restorePatient(id);

        // Assert
        assertNotNull(response);
        assertFalse(patient.isDeleted());
        Mockito.verify(patientRepository).save(patient);
    }

    @Test
    public void testRestorePatient_patientNotFound() {
        // Arrange
        Long id = 1L;

        Mockito.when(patientRepository.findByIdAndDeletedTrue(id))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(PatientNotFoundException.class, () -> patientService.restorePatient(id));
    }
}
