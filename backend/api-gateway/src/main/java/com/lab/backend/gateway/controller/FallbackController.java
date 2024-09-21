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

@RestController
@Log4j2
public class FallbackController {
    @RequestMapping(value = "/fallback/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackAuth() {
        try {
            throw new AuthServiceUnavailableException("Auth service is temporarily unavailable. Please try again later.");
        } finally {

        }
    }

    @RequestMapping(value = "/fallback/user", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackUser() {
        try {
            throw new UserServiceUnavailableException("User management service is temporarily unavailable. Please try again later.");
        } finally {

        }
    }

    @RequestMapping(value = "/fallback/patient", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackPatient() {
        try {
            throw new PatientServiceUnavailableException("Patient service is temporarily unavailable. Please try again later.");
        } finally {

        }
    }

    @RequestMapping(value = "/fallback/report", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackReport() {
        try {
            throw new ReportServiceUnavailableException("Report service is temporarily unavailable. Please try again later.");
        } finally {

        }
    }
}
