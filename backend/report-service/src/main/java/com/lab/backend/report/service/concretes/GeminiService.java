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

@Service
@RequiredArgsConstructor
@Log4j2
public class GeminiService {
    @Value("${gemini.api-key}")
    private String GEMINI_API_KEY;

    public String getInsight(String data) throws IOException {
        String ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + GEMINI_API_KEY;

        JsonObject requestJson = getJsonObject(data);

        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            String json = response.toString();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonArray candidatesArray = jsonObject.getAsJsonArray("candidates");
            JsonElement candidateElement = candidatesArray.get(0);
            JsonObject candidateObject = candidateElement.getAsJsonObject();
            JsonObject contentObject = candidateObject.getAsJsonObject("content");
            JsonArray newPartsArray = contentObject.getAsJsonArray("parts");

            JsonElement partElement = newPartsArray.get(0);
            JsonObject partObject = partElement.getAsJsonObject();

            return partObject.get("text").getAsString();
        } catch (Exception exception) {
            throw new GeminiConnectionFailedException("Gemini connection failed");
        }
    }

    private JsonObject getJsonObject(String data) {
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
        safetySettings.addProperty("thresold", "BLOCK_MEDIUM_AND_ABOVE");
        JsonArray safetySettingsArray = new JsonArray();
        safetySettingsArray.add(safetySettings);
        requestJson.add("safetySettings", safetySettingsArray);

        return requestJson;
    }

    private JsonArray getJsonElements(String data) {
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", "Suppose you have a patient report generation application. Please analyze the diagnosis details of the patients and make necessary recommendations for the patients. Suggest what patients should eat, what they should not eat, what they should do, what they should not do. Be creative. Use basic words. Give me a simple analysis. All of them will be as paragraphs, not item by item. Make it about 200 words.\n" + data);
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray partsArray = new JsonArray();
        partsArray.add(userPart);
        userContent.add("parts", partsArray);
        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userContent);
        return contentsArray;
    }
}
