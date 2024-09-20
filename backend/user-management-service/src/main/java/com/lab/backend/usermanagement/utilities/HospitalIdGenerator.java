package com.lab.backend.usermanagement.utilities;

import com.lab.backend.usermanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
@AllArgsConstructor
public class HospitalIdGenerator {
    private static final int HOSPITAL_ID_LENGTH = 7;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    private UserRepository userRepository;

    public String generateUniqueHospitalId() {
        String hospitalId;
        Set<String> existingIds = new HashSet<>(this.userRepository.findAllHospitalIdAndDeletedFalse());
        do {
            hospitalId = generateRandomHospitalId();
        } while (existingIds.contains(hospitalId));

        return hospitalId;
    }

    private String generateRandomHospitalId() {
        StringBuilder sb = new StringBuilder(HOSPITAL_ID_LENGTH);
        Random random = new Random();
        for (int i = 0; i < HOSPITAL_ID_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
