package com.lab.backend.patient.controller;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;
import com.lab.backend.patient.service.abstracts.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class PatientController {
    private final PatientService patientService;

    @GetMapping("/id/{id}")
    public ResponseEntity<GetPatientResponse> getPatientById(@PathVariable Long id) {
        GetPatientResponse response = this.patientService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trIdNumber")
    public ResponseEntity<GetPatientResponse> getPatientByTrIdNumber(@RequestParam String trIdNumber) {
        GetPatientResponse response = this.patientService.getPatientByTrIdNumber(trIdNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chronicDiseases/{id}")
    public ResponseEntity<Set<String>> getChronicDiseasesById(@PathVariable Long id) {
        Set<String> response = this.patientService.getChronicDiseasesById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filteredAndSorted")
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
        PagedResponse<GetPatientResponse> response = this.patientService.getAllPatientsFilteredAndSorted(page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, lastPatientRegistrationTime, deleted);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GetPatientResponse> savePatient(@RequestBody @Valid CreatePatientRequest createPatientRequest) {
        GetPatientResponse response = this.patientService.savePatient(createPatientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<GetPatientResponse> updatePatient(@RequestBody @Valid UpdatePatientRequest updatePatientRequest) {
        GetPatientResponse response = this.patientService.updatePatient(updatePatientRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        this.patientService.deletePatient(id);
        return ResponseEntity.ok("Patient has been successfully deleted.");
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<GetPatientResponse> restorePatient(@PathVariable Long id) {
        GetPatientResponse response = this.patientService.restorePatient(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
