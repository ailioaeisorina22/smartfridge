package com.smartfridge.dto;

import java.util.List;

public class PreferenceRequest {
    private String email;
    private List<String> preferences;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
