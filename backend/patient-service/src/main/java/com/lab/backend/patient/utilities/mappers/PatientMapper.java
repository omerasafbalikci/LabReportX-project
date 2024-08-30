package com.lab.backend.patient.utilities.mappers;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.entity.Patient;
import org.springframework.stereotype.Component;

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
        return new GetPatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getTrIdNumber(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getBloodType(),
                patient.getPhoneNumber(),
                patient.getEmail(),
                patient.getChronicDiseases(),
                patient.getUpdatedDate()
        );
    }
}
