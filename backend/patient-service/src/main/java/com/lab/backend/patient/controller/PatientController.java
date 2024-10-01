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

    @GetMapping("/id/{id}")
    public ResponseEntity<GetPatientResponse> getPatientById(@PathVariable Long id) {
        log.trace("Received request to get patient by id: {}", id);
        GetPatientResponse response = this.patientService.getPatientById(id);
        log.info("Successfully fetched patient with id: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tr-id-number")
    public ResponseEntity<GetPatientResponse> getPatientByTrIdNumber(@RequestParam String trIdNumber) {
        log.trace("Received request to get patient by TR ID number: {}", trIdNumber);
        GetPatientResponse response = this.patientService.getPatientByTrIdNumber(trIdNumber);
        log.info("Successfully fetched patient with TR ID number: {}", trIdNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chronic-diseases/{id}")
    public ResponseEntity<Set<String>> getChronicDiseasesById(@PathVariable Long id) {
        log.trace("Received request to get chronic diseases for patient id: {}", id);
        Set<String> response = this.patientService.getChronicDiseasesById(id);
        log.info("Successfully fetched chronic diseases for patient id: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email")
    public ResponseEntity<String> getEmail(@RequestParam String trIdNumber) {
        String response = this.patientService.getEmail(trIdNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-tc")
    public ResponseEntity<Boolean> checkTrIdNumber(@RequestParam String trIdNumber) {
        Boolean response = this.patientService.checkTrIdNumber(trIdNumber);
        return ResponseEntity.ok(response);
    }

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
        log.trace("Received request for filtered and sorted patients with parameters: page={}, size={}, sortBy={}, direction={}, firstName={}, lastName={}, trIdNumber={}, birthDate={}, gender={}, bloodType={}, phoneNumber={}, email={}, chronicDisease={}, lastPatientRegistrationTime={}, deleted={}",
                page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        PagedResponse<GetPatientResponse> response = this.patientService.getAllPatientsFilteredAndSorted(page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        log.info("Successfully fetched filtered and sorted patients.");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GetPatientResponse> savePatient(@RequestBody @Valid CreatePatientRequest createPatientRequest) {
        log.trace("Received request to save patient: {}", createPatientRequest);
        GetPatientResponse response = this.patientService.savePatient(createPatientRequest);
        log.info("Successfully saved patient: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<GetPatientResponse> updatePatient(@RequestBody @Valid UpdatePatientRequest updatePatientRequest) {
        log.trace("Received request to update patient: {}", updatePatientRequest);
        GetPatientResponse response = this.patientService.updatePatient(updatePatientRequest);
        log.info("Successfully updated patient: {}", response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        log.trace("Received request to delete patient with id: {}", id);
        this.patientService.deletePatient(id);
        log.info("Successfully deleted patient with id: {}", id);
        return ResponseEntity.ok("Patient has been successfully deleted.");
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<GetPatientResponse> restorePatient(@PathVariable Long id) {
        log.trace("Received request to restore patient with id: {}", id);
        GetPatientResponse response = this.patientService.restorePatient(id);
        log.info("Successfully restored patient with id: {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
