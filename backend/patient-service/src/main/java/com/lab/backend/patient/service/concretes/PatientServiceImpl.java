package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.requests.WeeklyStats;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.repository.PatientRepository;
import com.lab.backend.patient.repository.PatientSpecification;
import com.lab.backend.patient.service.abstracts.PatientService;
import com.lab.backend.patient.utilities.PatientAnalyticsProducer;
import com.lab.backend.patient.utilities.exceptions.PatientAlreadyExistsException;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final PatientAnalyticsProducer patientAnalyticsProducer;
    private static final String PATIENT_NOT_FOUND = "Patient not found with TR ID number: ";

    /**
     * Retrieves a patient's details by their ID.
     *
     * @param id the ID of the patient to retrieve
     * @return the patient details wrapped in a GetPatientResponse object
     * @throws PatientNotFoundException if no patient is found with the given ID
     */
    @Override
    @Cacheable(value = "patients", key = "#id", unless = "#result == null")
    public GetPatientResponse getPatientById(Long id) {
        log.trace("Entering getPatientById method in PatientServiceImpl");
        log.info("Fetching patient by ID: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("Patient not found with id: {}", id);
            return new PatientNotFoundException("Patient not found with id: " + id);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully fetched patient by ID: {}", id);
        log.trace("Exiting getPatientById method in PatientServiceImpl");
        return response;
    }

    /**
     * Retrieves a patient's details by their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient to retrieve
     * @return the patient details wrapped in a GetPatientResponse object
     * @throws PatientNotFoundException if no patient is found with the given TR ID number
     */
    @Override
    @Cacheable(value = "patients", key = "#trIdNumber", unless = "#result == null")
    public GetPatientResponse getPatientByTrIdNumber(String trIdNumber) {
        log.trace("Entering getPatientByTrIdNumber method in PatientServiceImpl");
        log.info("Fetching patient by TR ID number: {}", trIdNumber);
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with TR ID number: {}", trIdNumber);
            return new PatientNotFoundException(PATIENT_NOT_FOUND + trIdNumber);
        });
        GetPatientResponse response = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully fetched patient by TR ID number: {}", trIdNumber);
        log.trace("Exiting getPatientByTrIdNumber method in PatientServiceImpl");
        return response;
    }

    /**
     * Generates and sends weekly patient registration statistics to a Kafka topic.
     *
     * <p>This method calculates the number of patient registrations for each of the past
     * 7 days, builds a {@link WeeklyStats} object containing the statistics, and sends
     * it to the configured Kafka topic using {@link PatientAnalyticsProducer}.
     */
    @Override
    public void sendWeeklyPatientRegistrationStats() {
        log.trace("Entering sendWeeklyPatientRegistrationStats method in PatientServiceImpl");
        LocalDate today = LocalDate.now();
        Map<String, Long> dailyRegistrations = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            Long count = this.patientRepository.countByLastPatientRegistrationTimeBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            dailyRegistrations.put(date.toString(), count);
        }
        WeeklyStats weeklyStats = WeeklyStats.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .weeklyStats(dailyRegistrations)
                .build();
        this.patientAnalyticsProducer.sendPatientRegistrationStats("patient-stats", weeklyStats);
        log.trace("Exiting sendWeeklyPatientRegistrationStats method in PatientServiceImpl");
    }

    /**
     * Retrieves a set of chronic diseases associated with a patient by their ID.
     *
     * @param id the ID of the patient to retrieve chronic diseases for
     * @return a set of chronic diseases for the patient
     */
    @Override
    @Cacheable(value = "chronicDiseases", key = "#id", unless = "#result == null")
    public Set<String> getChronicDiseasesById(Long id) {
        log.trace("Entering getChronicDiseasesById method in PatientServiceImpl");
        log.info("Fetching chronic diseases by ID: {}", id);
        Set<String> chronicDiseases = this.patientRepository.findChronicDiseasesByIdAndDeletedFalse(id);
        if (chronicDiseases == null) {
            log.warn("No chronic diseases found for patient ID: {}", id);
            chronicDiseases = Collections.emptySet();
        }
        log.info("Successfully fetched chronic diseases by ID: {}", id);
        log.trace("Exiting getChronicDiseasesById method in PatientServiceImpl");
        return chronicDiseases;
    }

    /**
     * Retrieves the email of a patient based on their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return the email of the patient
     * @throws PatientNotFoundException if no patient is found with the given TR ID number
     */
    @Override
    public String getEmail(String trIdNumber) {
        log.trace("Entering getEmail method in PatientServiceImpl");
        log.info("Fetching email for patient with TR ID number: {}", trIdNumber);
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with TrIdNumber: {}", trIdNumber);
            return new PatientNotFoundException(PATIENT_NOT_FOUND + trIdNumber);
        });
        log.info("Successfully retrieved email for patient with TR ID number: {}", trIdNumber);
        log.trace("Exiting getEmail method in PatientServiceImpl");
        return patient.getEmail();
    }

    /**
     * Checks if a patient is registered within the last 6 hours based on their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return true if the patient is registered within the last 6 hours, false otherwise
     * @throws PatientNotFoundException if no patient is found with the given TR ID number
     */
    @Override
    public Boolean isPatientRegistered(String trIdNumber) {
        log.trace("Entering isPatientRegistered method in PatientServiceImpl");
        log.info("Checking registration time for patient with TR ID number: {}", trIdNumber);
        Patient patient = this.patientRepository.findByTrIdNumberAndDeletedFalse(trIdNumber).orElseThrow(() -> {
            log.error("Patient not found with TR ID number {}", trIdNumber);
            return new PatientNotFoundException(PATIENT_NOT_FOUND + trIdNumber);
        });
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime lastRegistrationTime = patient.getLastPatientRegistrationTime();
        Duration duration = Duration.between(lastRegistrationTime, currentTime);
        boolean isRegisteredRecently = !(duration.toHours() > 6);
        if (isRegisteredRecently) {
            log.info("Patient with TR ID number {} is registered within the last 6 hours.", trIdNumber);
        } else {
            log.info("Patient with TR ID number {} is not registered within the last 6 hours.", trIdNumber);
        }
        log.trace("Exiting isPatientRegistered method in PatientServiceImpl");
        return isRegisteredRecently;
    }

    /**
     * Retrieves a paginated list of patients filtered and sorted by the given parameters.
     *
     * @param page                        the page number to retrieve
     * @param size                        the number of patients per page
     * @param sortBy                      the field to sort by
     * @param direction                   the sorting direction (ASC or DESC)
     * @param firstName                   the first name filter
     * @param lastName                    the last name filter
     * @param trIdNumber                  the TR ID number filter
     * @param birthDate                   the date of birth filter
     * @param gender                      the gender filter
     * @param bloodType                   the blood type filter
     * @param phoneNumber                 the phone number filter
     * @param email                       the email filter
     * @param chronicDisease              the chronic disease filter
     * @param lastPatientRegistrationTime the last registration time filter
     * @param deleted                     the deletion status filter
     * @return a paginated response containing the filtered and sorted patient list
     */
    @Override
    public PagedResponse<GetPatientResponse> getAllPatientsFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                             String lastName, String trIdNumber, String birthDate, String gender,
                                                                             String bloodType, String phoneNumber, String email, String chronicDisease,
                                                                             String lastPatientRegistrationTime, Boolean deleted) {
        log.trace("Entering getAllPatientsFilteredAndSorted method in PatientServiceImpl");
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
        log.trace("Exiting getAllPatientsFilteredAndSorted method in PatientServiceImpl");
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

    /**
     * Saves a new patient or updates an existing patient's registration time.
     *
     * @param createPatientRequest the request object containing patient data
     * @return the saved patient details wrapped in a GetPatientResponse object
     */
    @Override
    @CachePut(value = "patients", key = "#result.id", unless = "#result == null")
    public GetPatientResponse savePatient(CreatePatientRequest createPatientRequest) {
        log.trace("Entering savePatient method in PatientServiceImpl");
        log.info("Saving patient with TR ID number: {}", createPatientRequest.getTrIdNumber());
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
        if (savedPatient.getEmail() != null) {
            this.patientAnalyticsProducer.sendEmail("patient-email-topic", savedPatient.getEmail());
        }
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(savedPatient);
        log.info("Successfully saved patient with ID: {}", savedPatient.getId());
        log.trace("Exiting savePatient method in PatientServiceImpl");
        return patientResponse;
    }

    /**
     * Updates an existing patient's details.
     *
     * @param updatePatientRequest the request object containing updated patient data
     * @return the updated patient details wrapped in a GetPatientResponse object
     * @throws PatientNotFoundException if no patient is found with the given ID
     */
    @Override
    @CachePut(value = "patients", key = "#result.id", unless = "#result == null")
    public GetPatientResponse updatePatient(UpdatePatientRequest updatePatientRequest) {
        log.trace("Entering updatePatient method in PatientServiceImpl");
        log.info("Updating patient with ID: {}", updatePatientRequest.getId());
        Patient existingPatient = this.patientRepository.findByIdAndDeletedFalse(updatePatientRequest.getId())
                .orElseThrow(() -> {
                    log.error("Patient does not exist with id: {}", updatePatientRequest.getId());
                    return new PatientNotFoundException("Patient does not exist with id: " + updatePatientRequest.getId());
                });
        if (updatePatientRequest.getTrIdNumber() != null && !updatePatientRequest.getTrIdNumber().equals(existingPatient.getTrIdNumber())) {
            boolean trIdExists = this.patientRepository.existsByTrIdNumberAndDeletedIsFalse(updatePatientRequest.getTrIdNumber());
            if (trIdExists) {
                log.error("Patient already exists with TR ID number: {}", updatePatientRequest.getTrIdNumber());
                throw new PatientAlreadyExistsException("Patient already exists with TR ID number: " + updatePatientRequest.getTrIdNumber());
            }
            existingPatient.setTrIdNumber(updatePatientRequest.getTrIdNumber());
        }
        updatePatientFieldIfNotNull(existingPatient::setFirstName, updatePatientRequest.getFirstName());
        updatePatientFieldIfNotNull(existingPatient::setLastName, updatePatientRequest.getLastName());
        updatePatientFieldIfNotNull(existingPatient::setBirthDate, updatePatientRequest.getBirthDate());
        updatePatientFieldIfNotNull(existingPatient::setGender, updatePatientRequest.getGender());
        updatePatientFieldIfNotNull(existingPatient::setBloodType, updatePatientRequest.getBloodType());
        updatePatientFieldIfNotNull(existingPatient::setPhoneNumber, updatePatientRequest.getPhoneNumber());
        updatePatientFieldIfNotNull(existingPatient::setEmail, updatePatientRequest.getEmail());
        updatePatientFieldIfNotNull(existingPatient::setChronicDiseases, updatePatientRequest.getChronicDiseases());
        this.patientRepository.save(existingPatient);
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(existingPatient);
        log.info("Successfully updated patient with ID: {}", updatePatientRequest.getId());
        log.trace("Exiting updatePatient method in PatientServiceImpl");
        return patientResponse;
    }

    private <T> void updatePatientFieldIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * Marks a patient as deleted, effectively soft deleting the record.
     *
     * @param id the ID of the patient to delete
     * @throws PatientNotFoundException if no patient is found with the given ID
     */
    @Override
    @CacheEvict(value = "patients", key = "#id")
    public void deletePatient(Long id) {
        log.trace("Entering deletePatient method in PatientServiceImpl");
        log.info("Deleting patient with ID: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
            log.error("Patient not found to delete. ID: {}", id);
            return new PatientNotFoundException("Patient not found to delete. ID: " + id);
        });
        patient.setDeleted(true);
        this.patientRepository.save(patient);
        log.info("Successfully deleted patient with ID: {}", id);
        log.trace("Exiting deletePatient method in PatientServiceImpl");
    }

    /**
     * Restores a soft-deleted patient record.
     *
     * @param id the ID of the patient to restore
     * @throws PatientNotFoundException if no patient is found with the given ID
     */
    @Override
    @CachePut(value = "patients", key = "#id", unless = "#result == null")
    public GetPatientResponse restorePatient(Long id) {
        log.trace("Entering restorePatient method in PatientServiceImpl");
        log.info("Restoring patient with ID: {}", id);
        Patient patient = this.patientRepository.findByIdAndDeletedTrue(id).orElseThrow(() -> {
            log.error("Patient not found to restore. ID: {}", id);
            return new PatientNotFoundException("Patient not found to restore. ID: " + id);
        });
        patient.setDeleted(false);
        this.patientRepository.save(patient);
        GetPatientResponse patientResponse = this.patientMapper.toGetPatientResponse(patient);
        log.info("Successfully restored patient with ID: {}", id);
        log.trace("Exiting restorePatient method in PatientServiceImpl");
        return patientResponse;
    }
}
