package com.lab.backend.patient.resource;

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

    @GetMapping("/{id}")
    public ResponseEntity<GetPatientResponse> getPatientById(@PathVariable Long id) {
        GetPatientResponse response = this.patientService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
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
            @RequestParam(required = false) String updatedDate,
            @RequestParam(required = false) Boolean deleted
    ) {
        PagedResponse<GetPatientResponse> response = this.patientService.getAllPatientsFilteredAndSorted(page, size, sortBy, direction, firstName, lastName, trIdNumber, birthDate, gender, bloodType, phoneNumber, email, chronicDisease, updatedDate, deleted);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GetPatientResponse> addPatient(@RequestBody @Valid CreatePatientRequest createPatientRequest) {
        GetPatientResponse response = this.patientService.addPatient(createPatientRequest);
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
