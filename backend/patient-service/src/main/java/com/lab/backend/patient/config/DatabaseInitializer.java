package com.lab.backend.patient.config;

import com.lab.backend.patient.dao.PatientRepository;
import com.lab.backend.patient.entity.BloodType;
import com.lab.backend.patient.entity.Gender;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.utilities.exceptions.UnexpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            initializePatients();
        } catch (Exception exception) {
            throw new UnexpectedException("Error during database initialization: " + exception.getMessage());
        }
    }

    private void initializePatients() {
        List<Patient> patients = List.of(
                Patient.builder()
                        .firstName("Ali")
                        .lastName("Ay")
                        .trIdNumber("12345678901")
                        .birthDate(LocalDate.of(1980, 1, 1))
                        .gender(Gender.MALE)
                        .bloodType(BloodType.A_POSITIVE)
                        .phoneNumber("05335468752")
                        .email("blkc.omerasaff@gmail.com")
                        .chronicDiseases(List.of("Yüksek tansiyon", "Diyabet"))
                        .updatedDate(LocalDateTime.now())
                        .build(),
                Patient.builder()
                        .firstName("Aylin")
                        .lastName("Say")
                        .trIdNumber("12345678902")
                        .birthDate(LocalDate.of(1980, 2, 2))
                        .gender(Gender.MALE)
                        .bloodType(BloodType.B_POSITIVE)
                        .phoneNumber("05335468758")
                        .email("blkc.omerasaff@gmail.com")
                        .chronicDiseases(List.of("Yüksek tansiyon", "Diyabet"))
                        .updatedDate(LocalDateTime.now())
                        .build()
        );

        for (Patient patient : patients) {
            if (!this.patientRepository.existsByTrIdNumberAndDeletedIsFalse(patient.getTrIdNumber())) {
                this.patientRepository.save(patient);
            }
        }
    }
}
