package com.lab.backend.patient.service.abstracts;

import com.lab.backend.patient.dto.responses.GetPatientResponse;

public interface BarcodeService {
    GetPatientResponse scanAndSavePatient();

    byte[] generateBarcodeForPatient(String trIdNumber);
}
