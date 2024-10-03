package com.lab.backend.gateway.controller;

import com.lab.backend.gateway.utilities.exceptions.AuthServiceUnavailableException;
import com.lab.backend.gateway.utilities.exceptions.PatientServiceUnavailableException;
import com.lab.backend.gateway.utilities.exceptions.ReportServiceUnavailableException;
import com.lab.backend.gateway.utilities.exceptions.UserServiceUnavailableException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * FallbackController provides fallback methods for each microservice in case of service unavailability.
 * When a microservice is down or unavailable, the Gateway redirects the request to the appropriate fallback method.
 *
 * @author Ömer Asaf BALIKÇI
 */

@RestController
@Log4j2
public class FallbackController {
    /**
     * Fallback method for the authentication service.
     * Triggered when the authentication service is unavailable.
     * Logs the error and throws an AuthServiceUnavailableException.
     *
     * @return ResponseEntity with a fallback message for the auth service.
     */
    @RequestMapping(value = "/fallback/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackAuth() {
        log.error("Auth service is unavailable, executing fallback method");
        try {
            throw new AuthServiceUnavailableException("Auth service is temporarily unavailable. Please try again later.");
        } finally {
            log.info("FallbackAuth executed");
        }
    }

    /**
     * Fallback method for the user management service.
     * Triggered when the user management service is unavailable.
     * Logs the error and throws a UserServiceUnavailableException.
     *
     * @return ResponseEntity with a fallback message for the user management service.
     */
    @RequestMapping(value = "/fallback/user", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackUser() {
        log.error("User service is unavailable, executing fallback method");
        try {
            throw new UserServiceUnavailableException("User management service is temporarily unavailable. Please try again later.");
        } finally {
            log.info("FallbackUser executed");
        }
    }

    /**
     * Fallback method for the patient service.
     * Triggered when the patient service is unavailable.
     * Logs the error and throws a PatientServiceUnavailableException.
     *
     * @return ResponseEntity with a fallback message for the patient service.
     */
    @RequestMapping(value = "/fallback/patient", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackPatient() {
        log.error("Patient service is unavailable, executing fallback method");
        try {
            throw new PatientServiceUnavailableException("Patient service is temporarily unavailable. Please try again later.");
        } finally {
            log.info("FallbackPatient executed");
        }
    }

    /**
     * Fallback method for the report service.
     * Triggered when the report service is unavailable.
     * Logs the error and throws a ReportServiceUnavailableException.
     *
     * @return ResponseEntity with a fallback message for the report service.
     */
    @RequestMapping(value = "/fallback/report", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackReport() {
        log.error("Report service is unavailable, executing fallback method");
        try {
            throw new ReportServiceUnavailableException("Report service is temporarily unavailable. Please try again later.");
        } finally {
            log.info("FallbackReport executed");
        }
    }
}
