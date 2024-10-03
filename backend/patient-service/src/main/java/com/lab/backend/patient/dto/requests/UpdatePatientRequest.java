package com.lab.backend.patient.dto.requests;

import com.lab.backend.patient.entity.BloodType;
import com.lab.backend.patient.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO to update patient.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePatientRequest {
    @NotNull(message = "Id must not be null")
    private Long id;
    private String firstName;
    private String lastName;
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Invalid TC number")
    private String trIdNumber;
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    private Gender gender;
    private BloodType bloodType;
    @Pattern(regexp = "^(?:\\+90|0)5[0-9]{2} ?[0-9]{3} ?[0-9]{2} ?[0-9]{2}$", message = "Invalid phone number")
    private String phoneNumber;
    @Email(message = "Please provide a valid email address")
    private String email;
    private Set<String> chronicDiseases;
}
