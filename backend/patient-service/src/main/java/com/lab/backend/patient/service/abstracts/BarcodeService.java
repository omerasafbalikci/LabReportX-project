package com.lab.backend.patient.service.abstracts;

import com.lab.backend.patient.dto.responses.GetPatientResponse;

/**
 * Service interface for handling barcode-related operations for patients.
 * This service provides methods for scanning and saving patient data, as well as generating barcodes for patients.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface BarcodeService {
    /**
     * Scans and saves patient data.
     *
     * @return a {@link GetPatientResponse} containing the saved patient's details
     */
    GetPatientResponse scanAndSavePatient();

    /**
     * Generates a barcode for a patient based on their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return a byte array representing the generated barcode
     */
    byte[] generateBarcodeForPatient(String trIdNumber);
}
