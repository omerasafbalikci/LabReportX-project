package com.lab.backend.report.service.concretes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lab.backend.report.utilities.exceptions.GeminiConnectionFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service class for interacting with the Gemini API to generate insights based on provided data.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class GeminiService {
    @Value("${gemini.api-key}")
    private String GEMINI_API_KEY;

    /**
     * Sends a request to the Gemini API to generate insights based on the provided data.
     *
     * @param data The input data for generating insights.
     * @return The generated insight as a string.
     * @throws IOException If there is an issue with the HTTP connection or reading the response.
     */
    public String getInsight(String data) throws IOException {
        log.trace("Entering getInsight method in GeminiService");
        String ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + GEMINI_API_KEY;

        JsonObject requestJson = getJsonObject(data);
        log.debug("Request JSON: {}", requestJson);
        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        log.info("Sending request to Gemini API at {}", ENDPOINT_URL);
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
            log.info("Request body sent: {}", requestJson.toString());
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            String json = response.toString();
            log.debug("Response JSON: {}", json);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonArray candidatesArray = jsonObject.getAsJsonArray("candidates");
            JsonElement candidateElement = candidatesArray.get(0);
            JsonObject candidateObject = candidateElement.getAsJsonObject();
            JsonObject contentObject = candidateObject.getAsJsonObject("content");
            JsonArray newPartsArray = contentObject.getAsJsonArray("parts");

            JsonElement partElement = newPartsArray.get(0);
            JsonObject partObject = partElement.getAsJsonObject();
            log.info("Insight generated successfully.");
            log.trace("Exiting getInsight method in GeminiService");
            return partObject.get("text").getAsString();
        } catch (Exception exception) {
            log.error("Gemini connection failed: {}", exception.getMessage());
            throw new GeminiConnectionFailedException("Gemini connection failed");
        }
    }

    /**
     * Constructs the JSON object required for the Gemini API request.
     *
     * @param data The input data for generating insights.
     * @return The constructed JSON object.
     */
    private JsonObject getJsonObject(String data) {
        log.trace("Entering getJsonObject method in GeminiService");
        JsonObject requestJson = new JsonObject();
        JsonArray contentsArray = getJsonElements(data);
        requestJson.add("contents", contentsArray);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 1);
        generationConfig.addProperty("topK", 0);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 8192);

        requestJson.add("generationConfig", generationConfig);

        JsonObject safetySettings = new JsonObject();
        safetySettings.addProperty("category", "HARM_CATEGORY_HARASSMENT");
        safetySettings.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        JsonArray safetySettingsArray = new JsonArray();
        safetySettingsArray.add(safetySettings);
        requestJson.add("safetySettings", safetySettingsArray);
        log.info("Generated JSON object for request: {}", requestJson);
        log.trace("Exiting getJsonObject method in GeminiService");
        return requestJson;
    }

    /**
     * Constructs the JSON elements array for the request.
     *
     * @param data The input data for generating insights.
     * @return The constructed JSON array of elements.
     */
    private JsonArray getJsonElements(String data) {
        log.trace("Entering getJsonElements method in GeminiService");
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", "You are working with a patient report generation system. Please analyze the diagnosis details provided below. Based on this, give simple recommendations for the patient's diet and lifestyle. Avoid listing items, and instead, write it as a short paragraph. Keep it concise, around 200 words.\n" + data);
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray partsArray = new JsonArray();
        partsArray.add(userPart);
        userContent.add("parts", partsArray);
        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userContent);
        log.info("Generated JSON elements for request: {}", contentsArray);
        log.trace("Exiting getJsonElements method in GeminiService");
        return contentsArray;
    }
}
