package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.repository.PatientRepository;
import com.lab.backend.patient.repository.PatientSpecification;
import com.lab.backend.patient.service.abstracts.PatientService;
import com.lab.backend.patient.utilities.exceptions.PatientNotFoundException;
import com.lab.backend.patient.utilities.mappers.PatientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Service implementation for managing patients.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    @Cacheable(value = "patients", key = "#id", unless = "#result == null")
    public GetPatientResponse getPatientById(Long id) {
        log.trace("Fetching patient by ID: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("Patient not found with id: {}", id);
            return new PatientNotFoundException("Patient not found with id: " + id);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully fetched patient by ID: {}", id);
        return response;
    }

    @Override
    @Cacheable(value = "patients", key = "#trIdNumber", unless = "#result == null")
    public GetPatientResponse getPatientByTrIdNumber(String trIdNumber) {
        log.trace("Fetching patient by TR ID number: {}", trIdNumber);
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with TR ID number: {}", trIdNumber);
            return new PatientNotFoundException("Patient not found with TR ID number: " + trIdNumber);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully fetched patient by TR ID number: {}", trIdNumber);
        return response;
    }

    @Override
    @Cacheable(value = "chronicDiseases", key = "#id", unless = "#result == null")
    public Set<String> getChronicDiseasesById(Long id) {
        log.trace("Fetching chronic diseases by ID: {}", id);
        Set<String> chronicDiseases = this.patientRepository.findChronicDiseasesByIdAndDeletedFalse(id);
        if (chronicDiseases == null) {
            log.warn("No chronic diseases found for patient ID: {}", id);
            chronicDiseases = Collections.emptySet();
        }
        log.info("Successfully fetched chronic diseases by ID: {}", id);
        return chronicDiseases;
    }

    @Override
    public String getEmail(String trIdNumber) {
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with TR ID number: {}", trIdNumber);
            return new PatientNotFoundException("Patient not found with TR ID number: " + trIdNumber);
        });
        return patient.getEmail();
    }

    @Override
    public Boolean checkTrIdNumber(String trIdNumber) {
        return this.patientRepository.existsByTrIdNumberAndDeletedIsFalse(trIdNumber);
    }

    @Override
    public PagedResponse<GetPatientResponse> getAllPatientsFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                             String lastName, String trIdNumber, String birthDate, String gender,
                                                                             String bloodType, String phoneNumber, String email, String chronicDisease,
                                                                             String lastPatientRegistrationTime, Boolean deleted) {
        log.debug("Fetching all patients with filters: page={}, size={}, sortBy={}, direction={}, firstName={}, lastName={}, trIdNumber={}, birthDate={}, gender={}, bloodType={}, phoneNumber={}, email={}, chronicDisease={}, lastPatientRegistrationTime={}, deleted={}",
                page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        PatientSpecification specification = new PatientSpecification(firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        Page<Patient> patientPage = this.patientRepository.findAll(specification, pagingSort);
        List<GetPatientResponse> patientResponses = patientPage.getContent()
                .stream()
                .map(this.patientMapper::toGetPatientResponse)
                .toList();
        log.info("Successfully fetched patients with filters: page={}, size={}, totalPages={}, totalElements={}", page, size, patientPage.getTotalPages(), patientPage.getTotalElements());
        return new PagedResponse<>(
                patientResponses,
                patientPage.getNumber(),
                patientPage.getTotalPages(),
                patientPage.getTotalElements(),
                patientPage.getSize(),
                patientPage.isFirst(),
                patientPage.isLast(),
                patientPage.hasNext(),
                patientPage.hasPrevious()
        );
    }

    @Override
    @CachePut(value = "patients", key = "#result.id", unless = "#result == null")
    public GetPatientResponse savePatient(CreatePatientRequest createPatientRequest) {
        log.trace("Saving patient with TR ID number: {}", createPatientRequest.getTrIdNumber());
        Patient savedPatient;
        Patient existingPatient = this.patientRepository.findByTrIdNumberAndDeletedFalse(createPatientRequest.getTrIdNumber())
                .orElse(null);

        if (existingPatient != null) {
            log.debug("Patient already exists. Updating registration time for TR ID number: {}", createPatientRequest.getTrIdNumber());
            existingPatient.setLastPatientRegistrationTime(LocalDateTime.now());
            savedPatient = this.patientRepository.save(existingPatient);
        } else {
            Patient patient = this.patientMapper.toPatient(createPatientRequest);
            patient.setLastPatientRegistrationTime(LocalDateTime.now());
            savedPatient = this.patientRepository.save(patient);
            log.info("New patient created with TR ID number: {}", createPatientRequest.getTrIdNumber());
        }
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(savedPatient);
        log.info("Successfully saved patient with ID: {}", savedPatient.getId());
        return patientResponse;
    }

    @Override
    @CachePut(value = "patients", key = "#result.id", unless = "#result == null")
    public GetPatientResponse updatePatient(UpdatePatientRequest updatePatientRequest) {
        log.trace("Updating patient with ID: {}", updatePatientRequest.getId());
        Patient existingPatient = this.patientRepository.findByIdAndDeletedFalse(updatePatientRequest.getId())
                .orElseThrow(() -> {
                    log.error("Patient does not exist with id: {}", updatePatientRequest.getId());
                    return new PatientNotFoundException("Patient does not exist with id: " + updatePatientRequest.getId());
                });
        updatePatientFieldIfNotNull(existingPatient::setFirstName, updatePatientRequest.getFirstName());
        updatePatientFieldIfNotNull(existingPatient::setLastName, updatePatientRequest.getLastName());
        updatePatientFieldIfNotNull(existingPatient::setTrIdNumber, updatePatientRequest.getTrIdNumber());
        updatePatientFieldIfNotNull(existingPatient::setBirthDate, updatePatientRequest.getBirthDate());
        updatePatientFieldIfNotNull(existingPatient::setGender, updatePatientRequest.getGender());
        updatePatientFieldIfNotNull(existingPatient::setBloodType, updatePatientRequest.getBloodType());
        updatePatientFieldIfNotNull(existingPatient::setPhoneNumber, updatePatientRequest.getPhoneNumber());
        updatePatientFieldIfNotNull(existingPatient::setEmail, updatePatientRequest.getEmail());
        updatePatientFieldIfNotNull(existingPatient::setChronicDiseases, updatePatientRequest.getChronicDiseases());
        this.patientRepository.save(existingPatient);
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(existingPatient);
        log.info("Successfully updated patient with ID: {}", updatePatientRequest.getId());
        return patientResponse;
    }

    private <T> void updatePatientFieldIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @Override
    @CacheEvict(value = "patients", key = "#id")
    public void deletePatient(Long id) {
        log.trace("Deleting patient with ID: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("Patient not found to delete. ID: {}", id);
            return new PatientNotFoundException("Patient not found to delete. ID: " + id);
        });
        patient.setDeleted(true);
        this.patientRepository.save(patient);
        log.info("Successfully deleted patient with ID: {}", id);
    }

    @Override
    @CachePut(value = "patients", key = "#id", unless = "#result == null")
    public GetPatientResponse restorePatient(Long id) {
        log.trace("Restoring patient with ID: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedTrue(id).orElseThrow(() -> {
            log.error("Patient not found to restore. ID: {}", id);
            return new PatientNotFoundException("Patient not found to restore. ID: " + id);
        });
        patient.setDeleted(false);
        this.patientRepository.save(patient);
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully restored patient with ID: {}", id);
        return patientResponse;
    }
}
