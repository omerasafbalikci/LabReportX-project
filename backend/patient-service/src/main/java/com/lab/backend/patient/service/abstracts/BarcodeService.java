package com.lab.backend.patient.service.abstracts;

import com.lab.backend.patient.dto.responses.GetPatientResponse;

public interface BarcodeService {
    GetPatientResponse scanAndFetchPatient();

    byte[] generateBarcodeForPatient(String trIdNumber);
}
