package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.dto.responses.GetPatientResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CacheService {
    @Cacheable(cacheNames = "patient_id", key = "#result.id", unless = "#result == null")
    public GetPatientResponse addPatientCache(GetPatientResponse patientResponse) {
        log.debug("Adding cache for patient with ID: {}", patientResponse.getId());
        return patientResponse;
    }

    @CachePut(cacheNames = "patient_id", key = "#result.id", unless = "#result == null")
    public GetPatientResponse updatePatientCache(GetPatientResponse patientResponse) {
        log.debug("Updating cache for patient with ID: {}", patientResponse.getId());
        return patientResponse;
    }
}
