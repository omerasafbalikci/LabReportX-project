package com.lab.backend.patient.utilities.mappers;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.entity.Patient;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between {@link CreatePatientRequest} and {@link Patient},
 * as well as between {@link Patient} and {@link GetPatientResponse}.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
public class PatientMapper {
    public Patient toPatient(CreatePatientRequest request) {
        if (request == null) {
            return null;
        }
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setTrIdNumber(request.getTrIdNumber());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodType(request.getBloodType());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setEmail(request.getEmail());
        patient.setChronicDiseases(request.getChronicDiseases());
        return patient;
    }

    public GetPatientResponse toGetPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }
        GetPatientResponse patientResponse = new GetPatientResponse();
        patientResponse.setId(patient.getId());
        patientResponse.setFirstName(patient.getFirstName());
        patientResponse.setLastName(patient.getLastName());
        patientResponse.setTrIdNumber(patient.getTrIdNumber());
        patientResponse.setBirthDate(patient.getBirthDate().toString());
        patientResponse.setGender(patient.getGender().toString());
        patientResponse.setBloodType(patient.getBloodType().toString());
        patientResponse.setPhoneNumber(patient.getPhoneNumber());
        patientResponse.setEmail(patient.getEmail());
        patientResponse.setLastPatientRegistrationTime(patient.getLastPatientRegistrationTime().toString());
        return patientResponse;
    }
}
