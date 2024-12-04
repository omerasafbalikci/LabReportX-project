package com.lab.backend.patient.service.abstracts;

import com.lab.backend.patient.dto.requests.CreatePatientRequest;
import com.lab.backend.patient.dto.requests.UpdatePatientRequest;
import com.lab.backend.patient.dto.responses.GetPatientResponse;
import com.lab.backend.patient.dto.responses.PagedResponse;

import java.util.Set;

/**
 * Interface for patient's service class.
 *
 * @author Ömer Asaf BALIKÇI
 */

public interface PatientService {
    /**
     * Retrieves patient details by ID.
     *
     * @param id the ID of the patient
     * @return a {@link GetPatientResponse} containing the patient's details
     */
    GetPatientResponse getPatientById(Long id);

    /**
     * Retrieves patient details by TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return a {@link GetPatientResponse} containing the patient's details
     */
    GetPatientResponse getPatientByTrIdNumber(String trIdNumber);

    /**
     * Generates and sends weekly patient registration statistics to a Kafka topic.
     */
    void sendWeeklyPatientRegistrationStats();

    /**
     * Retrieves the chronic diseases of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return a {@link Set} of chronic diseases, or an empty set if none are found
     */
    Set<String> getChronicDiseasesById(Long id);

    /**
     * Retrieves the email of a patient by their TR ID number.
     *
     * @param trIdNumber the TR ID number of the patient
     * @return the email of the patient
     */
    String getEmail(String trIdNumber);

    /**
     * Determines if a patient with the specified TR ID number is registered within the last 6 hours.
     * This method checks the registration time of the patient and returns true if the
     * patient was registered within the last 6 hours, and false if the registration
     * occurred more than 6 hours ago.
     *
     * @param trIdNumber the TR ID number of the patient to check
     * @return true if the patient is registered within the last 6 hours,
     * false if registered more than 6 hours ago
     */
    Boolean isPatientRegistered(String trIdNumber);

    /**
     * Retrieves all patients with filtering and sorting options.
     * This method supports pagination, sorting, and filtering by various patient attributes.
     *
     * @param page                        the page number to retrieve
     * @param size                        the number of items per page
     * @param sortBy                      the attribute to sort by
     * @param direction                   the sort direction (asc or desc)
     * @param firstName                   filter by first name
     * @param lastName                    filter by last name
     * @param trIdNumber                  filter by TR ID number
     * @param birthDate                   filter by date of birth
     * @param gender                      filter by gender
     * @param bloodType                   filter by blood type
     * @param phoneNumber                 filter by phone number
     * @param email                       filter by email
     * @param chronicDisease              filter by chronic disease
     * @param lastPatientRegistrationTime filter by the last patient registration time
     * @param deleted                     filter by deleted status
     * @return a paginated response containing the filtered and sorted list of patients
     */
    PagedResponse<GetPatientResponse> getAllPatientsFilteredAndSorted(int page, int size, String sortBy, String direction, String firstName,
                                                                      String lastName, String trIdNumber, String birthDate, String gender,
                                                                      String bloodType, String phoneNumber, String email, String chronicDisease,
                                                                      String lastPatientRegistrationTime, Boolean deleted);

    /**
     * Saves a new patient using the provided request data.
     *
     * @param createPatientRequest the data to create a new patient
     * @return a {@link GetPatientResponse} containing the saved patient's details
     */
    GetPatientResponse savePatient(CreatePatientRequest createPatientRequest);

    /**
     * Updates an existing patient using the provided request data.
     *
     * @param updatePatientRequest the data to update the patient
     * @return a {@link GetPatientResponse} containing the updated patient's details
     */
    GetPatientResponse updatePatient(UpdatePatientRequest updatePatientRequest);

    /**
     * Soft-deletes a patient by their ID.
     *
     * @param id the ID of the patient to delete
     */
    void deletePatient(Long id);

    /**
     * Restores a previously soft-deleted patient by their ID.
     *
     * @param id the ID of the patient to restore
     * @return a {@link GetPatientResponse} containing the restored patient's details
     */
    GetPatientResponse restorePatient(Long id);
}
