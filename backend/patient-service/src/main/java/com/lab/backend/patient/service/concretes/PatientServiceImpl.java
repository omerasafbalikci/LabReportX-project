package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.dao.PatientRepository;
import com.lab.backend.patient.dao.PatientSpecification;
import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.service.abstracts.PatientService;
import com.lab.backend.patient.utilities.exceptions.PatientNotFoundException;
import com.lab.backend.patient.utilities.mappers.PatientMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final CacheService cacheService;

    @Override
    @Cacheable(value = "patient_id", key = "#id", unless = "#result == null")
    public GetPatientResponse getPatientById(Long id) {
        log.trace("Entering getPatientById method in PatientServiceImpl with id: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("Patient not found with id: {}", id);
            return new PatientNotFoundException("Patient not found with id: " + id);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.debug("Successfully retrieved patient with id: {}", id);
        log.trace("Exiting getPatientById method in PatientServiceImpl with id: {}", id);
        return response;
    }

    @Override
    public GetPatientResponse getPatientByTrIdNumber(String trIdNumber) {
        log.trace("Entering getPatientByTrIdNumber method in PatientServiceImpl with trIdNumber: {}", trIdNumber);
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with trIdNumber: {}", trIdNumber);
            return new PatientNotFoundException("Patient not found with trIdNumber: " + trIdNumber);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.debug("Successfully retrieved patient with trIdNumber: {}", trIdNumber);
        log.trace("Exiting getPatientByTrIdNumber method in PatientServiceImpl with trIdNumber: {}", trIdNumber);
        return response;
    }

    @Override
    public PagedResponse<GetPatientResponse> getAllPatientsFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                             String lastName, String trIdNumber, String birthDate, String gender,
                                                                             String bloodType, String phoneNumber, String email, String chronicDisease,
                                                                             String updatedDate, Boolean deleted) {
        Pageable pagingSort = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        PatientSpecification specification = new PatientSpecification(firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, updatedDate, deleted);
        Page<Patient> patientPage = this.patientRepository.findAll(specification, pagingSort);
        List<GetPatientResponse> patientResponses = patientPage.getContent()
                .stream()
                .map(this.patientMapper::toGetPatientResponse)
                .toList();

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
    public GetPatientResponse savePatient(CreatePatientRequest createPatientRequest) {
        Patient savedPatient;
        if (this.patientRepository.existsByTrIdNumberAndDeletedIsFalse(createPatientRequest.getTrIdNumber())) {
            Patient existingPatient = this.patientRepository.findByTrIdNumberAndDeletedFalse(createPatientRequest.getTrIdNumber())
                    .orElseThrow(() -> {
                        log.error("Patient not found to save. ID: {}", createPatientRequest.getTrIdNumber());
                        return new PatientNotFoundException("Patient not found to save. ID: " + createPatientRequest.getTrIdNumber());
                    });
            savedPatient = this.patientRepository.save(existingPatient);
        } else {
            Patient patient = this.patientMapper.toPatient(createPatientRequest);
            savedPatient = this.patientRepository.save(patient);
        }
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(savedPatient);
        this.cacheService.savePatientCache(patientResponse);
        return patientResponse;
    }

    @Override
    public GetPatientResponse updatePatient(UpdatePatientRequest updatePatientRequest) {
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
        Patient savedPatient = this.patientRepository.save(existingPatient);
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(savedPatient);
        this.cacheService.updatePatientCache(patientResponse);
        return patientResponse;
    }

    private <T> void updatePatientFieldIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @Override
    @CacheEvict(value = "patient_id", key = "#id")
    public void deletePatient(Long id) {
        Patient patient = this.patientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.error("Patient not found to delete. ID: {}", id);
                    return new PatientNotFoundException("Patient not found to delete. ID: " + id);
                });
        patient.setDeleted(true);
        this.patientRepository.save(patient);
    }

    @Override
    public GetPatientResponse restorePatient(Long id) {
        Patient patient = this.patientRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> {
                    log.error("Patient not found to restore. ID: {}", id);
                    return new PatientNotFoundException("Patient not found to restore. ID: " + id);
                });
        patient.setDeleted(false);
        Patient savedPatient = this.patientRepository.save(patient);
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(savedPatient);
        this.cacheService.savePatientCache(patientResponse);
        return patientResponse;
    }
}
