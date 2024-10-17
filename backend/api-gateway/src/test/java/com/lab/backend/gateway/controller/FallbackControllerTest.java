package com.lab.backend.gateway.controller;

import com.lab.backend.gateway.utilities.exceptions.AuthServiceUnavailableException;
import com.lab.backend.gateway.utilities.exceptions.PatientServiceUnavailableException;
import com.lab.backend.gateway.utilities.exceptions.ReportServiceUnavailableException;
import com.lab.backend.gateway.utilities.exceptions.UserServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class FallbackControllerTest {
    @InjectMocks
    private FallbackController fallbackController;

    @Test
    void whenFallbackAuthEndpointIsCalled_thenAuthServiceUnavailableExceptionIsThrown() {
        // Act & Assert
        Exception exception = assertThrows(AuthServiceUnavailableException.class,
                () -> fallbackController.fallbackAuth());

        assertEquals("Auth service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void whenFallbackUserEndpointIsCalled_thenUserServiceUnavailableExceptionIsThrown() {
        // Act & Assert
        Exception exception = assertThrows(UserServiceUnavailableException.class,
                () -> fallbackController.fallbackUser());

        assertEquals("User management service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void whenFallbackPatientEndpointIsCalled_thenPatientServiceUnavailableExceptionIsThrown() {
        // Act & Assert
        Exception exception = assertThrows(PatientServiceUnavailableException.class,
                () -> fallbackController.fallbackPatient());

        assertEquals("Patient service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void whenFallbackReportEndpointIsCalled_thenReportServiceUnavailableExceptionIsThrown() {
        // Act & Assert
        Exception exception = assertThrows(ReportServiceUnavailableException.class,
                () -> fallbackController.fallbackReport());

        assertEquals("Report service is temporarily unavailable. Please try again later.", exception.getMessage());
    }
}
