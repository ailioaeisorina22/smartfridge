package com.smartfridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String generateContent(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey; // Updated model name to correct endpoint

        // Construct request body
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = rootNode.putArray("contents");
        ObjectNode contentNode = contentsArray.addObject();
        ArrayNode partsArray = contentNode.putArray("parts");
        ObjectNode partNode = partsArray.addObject();
        partNode.put("text", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);
            JsonNode body = response.getBody();
            if (body != null && body.has("candidates") && body.get("candidates").isArray() && body.get("candidates").size() > 0) {
                JsonNode content = body.get("candidates").get(0).get("content");
                if (content != null && content.has("parts") && content.get("parts").isArray() && content.get("parts").size() > 0) {
                    return content.get("parts").get(0).get("text").asText();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare la apelarea Gemini API: " + e.getMessage());
        }

        throw new RuntimeException("Eroare la procesarea raspunsului de la Gemini.");
    }
}
