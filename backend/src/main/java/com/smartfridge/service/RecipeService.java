package com.smartfridge.service;

import com.smartfridge.dto.RecipeRequest;
import com.smartfridge.dto.RecipeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {

    private final AiService aiService;
    private final EmailService emailService;
    private final PreferenceService preferenceService;

    public RecipeService(AiService aiService, EmailService emailService, PreferenceService preferenceService) {
        this.aiService = aiService;
        this.emailService = emailService;
        this.preferenceService = preferenceService;
    }

    public RecipeResponse processAction(RecipeRequest request) {
        String ingredients = request.getIngredients();
        String email = request.getEmail();
        String action = request.getAction();

        if (ingredients == null || email == null || action == null) {
            throw new IllegalArgumentException("Date incomplete");
        }

        List<String> prefs = preferenceService.getPreferences(email);
        String prefString = prefs.isEmpty() ? "" : " Te rog să respecți cu strictețe aceste preferințe alimentare: " + String.join(", ", prefs) + ".";

        if ("recipe".equals(action)) {
            String prompt = String.format("Creează o scurtă rețetă delicioasă folosind următoarele ingrediente: %s.%s\n" +
                    "      Te rog să formatezi răspunsul în HTML curat (folosind <h3>, <ul>, <li>, <p>, <strong>), fără tag-uri markdown, fără ```html.", ingredients, prefString);
            String recipeHtml = aiService.generateContent(prompt);
            return new RecipeResponse(recipeHtml, null, null);
        }

        if ("shopping_list".equals(action)) {
            String prompt = String.format("Având următoarele ingrediente în frigider: %s.%s\n" +
                    "      Gândește-te la o rețetă pe care aș putea să o fac și creează DOAR o listă de cumpărături cu ingredientele suplimentare de care aș mai avea nevoie.\n" +
                    "      Înainte de listă, adaugă un scurt paragraf prietenos în care menționezi numele rețetei propuse și faptul că ai ținut cont de preferințe (dacă au fost selectate).\n" +
                    "      IMPORTANT: NU include pașii de preparare sau instrucțiunile rețetei! Emailul trebuie să conțină STRICT introducerea și ingredientele care lipsesc.\n" +
                    "      Te rog să formatezi răspunsul în HTML curat (folosind <h2>, <h3>, <ul>, <li>, <p>, <strong>), fără tag-uri markdown, fără ```html.", ingredients, prefString);
            String listHtml = aiService.generateContent(prompt);
            
            emailService.sendEmail(email, "Lista ta de cumpărături inteligentă \uD83D\uDED2", listHtml);

            return new RecipeResponse(null, "Lista cu ce mai e nevoie de cumparat a fost trimisa pe email!", null);
        }

        throw new IllegalArgumentException("Acțiune invalidă");
    }
}
