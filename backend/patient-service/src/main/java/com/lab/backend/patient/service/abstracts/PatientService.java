package com.lab.backend.patient.service.abstracts;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;

/**
 * Interface for patient's service class.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface PatientService {
    GetPatientResponse getPatientById(Long id);

    PagedResponse<GetPatientResponse> getAllPatientsFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                      String lastName, String trIdNumber, String birthDate, String gender,
                                                                      String bloodType, String phoneNumber, String email, String chronicDisease,
                                                                      String updatedDate, Boolean deleted);

    GetPatientResponse addPatient(CreatePatientRequest createPatientRequest);

    GetPatientResponse updatePatient(UpdatePatientRequest updatePatientRequest);

    void deletePatient(Long id);

    GetPatientResponse restorePatient(Long id);
}
