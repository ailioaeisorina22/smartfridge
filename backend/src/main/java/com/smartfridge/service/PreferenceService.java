package com.smartfridge.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PreferenceService {

    private final Map<String, List<String>> userPreferences = new ConcurrentHashMap<>();

    public List<String> getPreferences(String email) {
        return userPreferences.getOrDefault(email, new ArrayList<>());
    }

    public void savePreferences(String email, List<String> preferences) {
        if (email != null && preferences != null) {
            userPreferences.put(email, preferences);
        }
    }
}
