package com.lab.backend.patient.controller;

import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.service.concretes.BarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/barcode")
@RequiredArgsConstructor
public class BarcodeController {
    private final BarcodeService barcodeService;

    @GetMapping("/scan")
    public ResponseEntity<GetPatientResponse> scanAndFetchPatient() {
        GetPatientResponse response = this.barcodeService.scanAndFetchPatient();
        return ResponseEntity.ok(response);
    }
}
