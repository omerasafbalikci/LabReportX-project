package com.lab.backend.report.utilities;

import com.lab.backend.report.repository.ReportRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
public class FileNumberGenerator {
    private static final int FILE_NUMBER_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    private ReportRepository reportRepository;

    public String generateUniqueFileNumber() {
        String fileNumber;
        List<String> existingNumbers = new ArrayList<>(this.reportRepository.findAllFileNumberAndDeletedFalse());
        do {
            fileNumber = generateRandomFileNumber();
        } while (existingNumbers.contains(fileNumber));

        return fileNumber;
    }

    private String generateRandomFileNumber() {
        StringBuilder sb = new StringBuilder(FILE_NUMBER_LENGTH);
        Random random = new Random();
        for (int i = 0; i < FILE_NUMBER_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
