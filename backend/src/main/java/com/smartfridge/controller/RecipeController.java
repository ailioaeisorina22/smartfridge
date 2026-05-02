package com.smartfridge.controller;

import com.smartfridge.dto.RecipeRequest;
import com.smartfridge.dto.RecipeResponse;
import com.smartfridge.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/generate-recipe")
    public ResponseEntity<RecipeResponse> generateRecipe(@RequestBody RecipeRequest request) {
        try {
            RecipeResponse response = recipeService.processAction(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new RecipeResponse(null, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new RecipeResponse(null, null, e.getMessage()));
        }
    }
}
