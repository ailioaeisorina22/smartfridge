package com.smartfridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        String url = "https://api.resend.com/emails";

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("from", "Smart Fridge <onboarding@resend.dev>");
        rootNode.put("to", to);
        rootNode.put("subject", subject);
        rootNode.put("html", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Eroare la trimiterea emailului");
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare la apelarea Resend API: " + e.getMessage());
        }
    }
}
