package com.smartfridge.controller;

import com.smartfridge.dto.PreferenceRequest;
import com.smartfridge.service.PreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<List<String>> getPreferences(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(preferenceService.getPreferences(email));
    }

    @PutMapping
    public ResponseEntity<Void> updatePreferences(@RequestBody PreferenceRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        preferenceService.savePreferences(request.getEmail(), request.getPreferences());
        return ResponseEntity.ok().build();
    }
}
