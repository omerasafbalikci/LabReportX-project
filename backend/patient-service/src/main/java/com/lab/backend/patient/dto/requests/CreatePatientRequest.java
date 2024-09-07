package com.lab.backend.patient.dto.requests;

import com.lab.backend.patient.entity.BloodType;
import com.lab.backend.patient.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for patient used as input.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePatientRequest {
    @NotBlank(message = "First name must not be blank")
    private String firstName;
    @NotBlank(message = "Last name must not be blank")
    private String lastName;
    @NotBlank(message = "TC must not be blank")
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Invalid TC number")
    private String trIdNumber;
    @NotNull(message = "Birth date must not be null")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    @NotNull(message = "Gender must not be null")
    private Gender gender;
    private BloodType bloodType;
    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "^(?:\\+90|0)5[0-9]{2} ?[0-9]{3} ?[0-9]{2} ?[0-9]{2}$", message = "Invalid phone number")
    private String phoneNumber;
    @Email(message = "Please provide a valid email address")
    private String email;
    private Set<String> chronicDiseases;
}
