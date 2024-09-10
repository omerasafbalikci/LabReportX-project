package com.lab.backend.usermanagement.utilities;

import com.lab.backend.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HospitalIdGenerator {
    private static final int HOSPITAL_ID_LENGTH = 7;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    private UserRepository userRepository;

    public String generateUniqueHospital
}
