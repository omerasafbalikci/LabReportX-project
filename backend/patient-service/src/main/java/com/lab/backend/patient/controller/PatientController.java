package com.lab.backend.patient.controller;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.service.abstracts.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST controller for managing patients.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Log4j2
public class PatientController {
    private final PatientService patientService;

    /**
     * Retrieves a patient by their ID.
     *
     * @param id the ID of the patient
     * @return ResponseEntity containing the {@link GetPatientResponse}
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<GetPatientResponse> getPatientById(@PathVariable("id") Long id) {
        log.trace("Entering getPatientById method in PatientController class");
        GetPatientResponse response = this.patientService.getPatientById(id);
        log.info("Successfully fetched patient with id: {}", id);
        log.trace("Exiting getPatientById method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a patient by their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return ResponseEntity containing the {@link GetPatientResponse}
     */
    @GetMapping("/tr-id-number")
    public ResponseEntity<GetPatientResponse> getPatientByTrIdNumber(@RequestParam String trIdNumber) {
        log.trace("Entering getPatientByTrIdNumber method in PatientController class");
        GetPatientResponse response = this.patientService.getPatientByTrIdNumber(trIdNumber);
        log.info("Successfully fetched patient with TR ID number: {}", trIdNumber);
        log.trace("Exiting getPatientByTrIdNumber method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves chronic diseases for a patient by their ID.
     *
     * @param id the ID of the patient
     * @return ResponseEntity containing a set of chronic diseases
     */
    @GetMapping("/chronic-diseases/{id}")
    public ResponseEntity<Set<String>> getChronicDiseasesById(@PathVariable("id") Long id) {
        log.trace("Entering getChronicDiseasesById method in PatientController class");
        Set<String> response = this.patientService.getChronicDiseasesById(id);
        log.info("Successfully fetched chronic diseases for patient id: {}", id);
        log.trace("Exiting getChronicDiseasesById method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the email of a patient by their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return ResponseEntity containing the email of the patient
     */
    @GetMapping("/email")
    public ResponseEntity<String> getEmail(@RequestParam String trIdNumber) {
        log.trace("Entering getEmail method in PatientController class");
        String response = this.patientService.getEmail(trIdNumber);
        log.info("Successfully fetched email for TR ID number: {}", trIdNumber);
        log.trace("Exiting getEmail method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Checks if a patient is registered with the specified TR ID number.
     * This endpoint receives a TR ID number as a request parameter and
     * returns a boolean value indicating whether the patient is registered
     * within the last 6 hours.
     *
     * @param trIdNumber the TR ID number of the patient to check
     * @return ResponseEntity containing a boolean value:
     * true if the patient is registered within the last 6 hours,
     * false otherwise
     */
    @GetMapping("/check-tr-id-number")
    public ResponseEntity<Boolean> isPatientRegistered(@RequestParam String trIdNumber) {
        log.trace("Entering isPatientRegistered method in PatientController class");
        Boolean response = this.patientService.isPatientRegistered(trIdNumber);
        log.info("TR ID number check for {} returned: {}", trIdNumber, response);
        log.trace("Exiting isPatientRegistered method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves filtered and sorted list of patients based on various parameters.
     *
     * @param page                        the page number
     * @param size                        the size of the page
     * @param sortBy                      the field to sort by
     * @param direction                   the direction of sorting (ASC or DESC)
     * @param firstName                   the first name of the patient
     * @param lastName                    the last name of the patient
     * @param trIdNumber                  the TR ID number of the patient
     * @param birthDate                   the date of birth of the patient
     * @param gender                      the gender of the patient
     * @param bloodType                   the blood type of the patient
     * @param phoneNumber                 the phone number of the patient
     * @param email                       the email of the patient
     * @param chronicDisease              the chronic disease of the patient
     * @param lastPatientRegistrationTime the last registration time of the patient
     * @param deleted                     whether the patient is deleted
     * @return ResponseEntity containing the paged and filtered list of patients
     */
    @GetMapping("/filtered-and-sorted")
    public ResponseEntity<PagedResponse<GetPatientResponse>> getAllPatientsFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String trIdNumber,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String chronicDisease,
            @RequestParam(required = false) String lastPatientRegistrationTime,
            @RequestParam(required = false) Boolean deleted
    ) {
        log.trace("Entering getAllPatientsFilteredAndSorted method in PatientController class");
        log.info("Received request for filtered and sorted patients with parameters: page={}, size={}, sortBy={}, direction={}, firstName={}, lastName={}, trIdNumber={}, birthDate={}, gender={}, bloodType={}, phoneNumber={}, email={}, chronicDisease={}, lastPatientRegistrationTime={}, deleted={}",
                page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        PagedResponse<GetPatientResponse> response = this.patientService.getAllPatientsFilteredAndSorted(page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        log.info("Successfully fetched filtered and sorted patients.");
        log.trace("Exiting getAllPatientsFilteredAndSorted method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Saves a new patient to the system.
     *
     * @param createPatientRequest the request containing the patient details
     * @return ResponseEntity containing the saved patient's details
     */
    @PostMapping
    public ResponseEntity<GetPatientResponse> savePatient(@RequestBody @Valid CreatePatientRequest createPatientRequest) {
        log.trace("Entering savePatient method in PatientController class");
        GetPatientResponse response = this.patientService.savePatient(createPatientRequest);
        log.info("Successfully saved patient: {}", response);
        log.trace("Exiting savePatient method in PatientController class");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing patient in the system.
     *
     * @param updatePatientRequest the request containing the updated patient details
     * @return ResponseEntity containing the updated patient's details
     */
    @PutMapping
    public ResponseEntity<GetPatientResponse> updatePatient(@RequestBody @Valid UpdatePatientRequest updatePatientRequest) {
        log.trace("Entering updatePatient method in PatientController class");
        GetPatientResponse response = this.patientService.updatePatient(updatePatientRequest);
        log.info("Successfully updated patient: {}", response);
        log.trace("Exiting updatePatient method in PatientController class");
        return ResponseEntity.ok(response);
    }

    /**
     * Soft deletes a patient by their ID.
     *
     * @param id the ID of the patient to be deleted
     * @return ResponseEntity containing a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable("id") Long id) {
        log.trace("Entering deletePatient method in PatientController class");
        this.patientService.deletePatient(id);
        log.info("Successfully deleted patient with id: {}", id);
        log.trace("Exiting deletePatient method in PatientController class");
        return ResponseEntity.ok("Patient has been successfully deleted.");
    }

    /**
     * Restores a soft-deleted patient by their ID.
     *
     * @param id the ID of the patient to be restored
     * @return ResponseEntity containing the restored patient's details
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<GetPatientResponse> restorePatient(@PathVariable("id") Long id) {
        log.trace("Entering restorePatient method in PatientController class");
        GetPatientResponse response = this.patientService.restorePatient(id);
        log.info("Successfully restored patient with id: {}", id);
        log.trace("Exiting restorePatient method in PatientController class");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
