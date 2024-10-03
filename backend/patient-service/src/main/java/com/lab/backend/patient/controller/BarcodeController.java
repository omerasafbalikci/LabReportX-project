package com.lab.backend.patient.controller;

import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.service.abstracts.BarcodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class responsible for handling barcode-related operations such as scanning
 * and saving a patient, and generating barcodes based on TR ID numbers.
 * Provides endpoints for scanning barcodes and generating barcodes as images.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@RequestMapping("/barcode")
@RequiredArgsConstructor
@Log4j2
public class BarcodeController {
    private final BarcodeService barcodeService;

    /**
     * Endpoint to scan a barcode, fetch the patient details, and save the patient data.
     * Uses the barcode scanner to retrieve patient information and logs the process at info level.
     *
     * @return a ResponseEntity containing the patient details as {@link GetPatientResponse}
     */
    @GetMapping("/scan")
    public ResponseEntity<GetPatientResponse> scanAndSavePatient() {
        log.trace("Entering scanAndSavePatient method.");
        log.info("Received request to scan and fetch patient.");
        GetPatientResponse response = this.barcodeService.scanAndSavePatient();
        log.info("Successfully fetched patient: {}", response);
        log.trace("Exiting scanAndSavePatient method.");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to generate a barcode for a patient based on their TR ID number.
     * The barcode is returned as a PNG image in the response body.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return a ResponseEntity containing the barcode image as a byte array or an error status
     */
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateBarcodeForPatient(@RequestParam String trIdNumber) {
        log.trace("Entering generateBarcodeForPatient method.");
        log.debug("Received request to generate barcode for TR ID number: {}", trIdNumber);
        try {
            byte[] barcode = this.barcodeService.generateBarcodeForPatient(trIdNumber);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "image/png");
            headers.setContentLength(barcode.length);
            log.debug("Barcode generated successfully for TR ID number: {}", trIdNumber);
            log.trace("Exiting generateBarcodeForPatient method.");
            return new ResponseEntity<>(barcode, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while generating barcode for TR ID number: {}", trIdNumber, e);
            log.trace("Exiting generateBarcodeForPatient method with error.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
