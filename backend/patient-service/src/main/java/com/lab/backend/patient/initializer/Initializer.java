package com.lab.backend.patient.initializer;

import com.lab.backend.patient.entity.BloodType;
import com.lab.backend.patient.entity.Gender;
import com.lab.backend.patient.entity.Patient;
import com.lab.backend.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializePatient();
    }

    private void initializePatient() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        boolean result = patientRepository.existsByTrIdNumberAndDeletedIsFalse("12345678912");
        if (!result) {
            this.patientRepository.save(Patient.builder()
                    .firstName("Ali")
                    .lastName("Ay")
                    .trIdNumber("12345678912")
                    .birthDate(LocalDate.parse("10-10-1970", formatter))
                    .gender(Gender.MALE)
                    .bloodType(BloodType.A_POSITIVE)
                    .phoneNumber("05468975236")
                    .email("blkc.omerasaff@gmail.com")
                    .chronicDiseases(Set.of("Astım", "Şeker"))
                    .lastPatientRegistrationTime(LocalDateTime.now())
                    .build());
        }

        if (!patientRepository.existsByTrIdNumberAndDeletedIsFalse("12345678913")) {
            this.patientRepository.save(Patient.builder()
                    .firstName("Ayşe")
                    .lastName("Akın")
                    .trIdNumber("12345678913")
                    .birthDate(LocalDate.parse("10-10-1980", formatter))
                    .gender(Gender.FEMALE)
                    .bloodType(BloodType.B_POSITIVE)
                    .phoneNumber("05468975238")
                    .email("ayseakin@gmail.com")
                    .chronicDiseases(Set.of("Astım", "Kalp"))
                    .lastPatientRegistrationTime(LocalDateTime.now())
                    .build());
        }

        if (!patientRepository.existsByTrIdNumberAndDeletedIsFalse("12345678914")) {
            this.patientRepository.save(Patient.builder()
                    .firstName("Kaan")
                    .lastName("Ateş")
                    .trIdNumber("12345678914")
                    .birthDate(LocalDate.parse("10-10-1990", formatter))
                    .gender(Gender.MALE)
                    .bloodType(BloodType.AB_NEGATIVE)
                    .phoneNumber("05468975239")
                    .email("kaanates@gmail.com")
                    .chronicDiseases(Set.of("Kalp", "Şeker"))
                    .lastPatientRegistrationTime(LocalDateTime.now())
                    .build());
        }
    }
}
