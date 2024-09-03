package com.lab.backend.patient.service.concretes;

import com.lab.backend.patient.dto.responses.GetPatientResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CacheService {
    @Cacheable(cacheNames = "patient_id", key = "#patientResponse.id", unless = "#result == null")
    public GetPatientResponse savePatientCache(GetPatientResponse patientResponse) {
        log.trace("Entering createProductCache method in CacheService class");
        log.debug("Creating cache for product with ID: {}", patientResponse.getId());

        log.trace("Exiting createProductCache method in CacheService class");
        return patientResponse;
    }

    @CachePut(cacheNames = "patient_id", key = "#patientResponse.id", unless = "#result == null")
    public GetPatientResponse updatePatientCache(GetPatientResponse patientResponse) {
        log.trace("Entering updateProductCache method in CacheService class");
        log.debug("Updating cache for product with ID: {}", patientResponse.getId());

        log.trace("Exiting updateProductCache method in CacheService class");
        return patientResponse;
    }
}
