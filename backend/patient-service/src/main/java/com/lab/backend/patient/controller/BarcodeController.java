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

@RestController
@RequestMapping("/barcode")
@RequiredArgsConstructor
@Log4j2
public class BarcodeController {
    private final BarcodeService barcodeService;

    @GetMapping("/scan")
    public ResponseEntity<GetPatientResponse> scanAndSavePatient() {
        log.info("Received request to scan and fetch patient.");
        GetPatientResponse response = this.barcodeService.scanAndSavePatient();
        log.info("Successfully fetched patient: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateBarcodeForPatient(@RequestParam String trIdNumber) {
        log.debug("Received request to generate barcode for TR ID number: {}", trIdNumber);
        try {
            byte[] barcode = this.barcodeService.generateBarcodeForPatient(trIdNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "image/png");
            headers.setContentLength(barcode.length);

            return new ResponseEntity<>(barcode, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while generating barcode.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
