package com.smartfridge.dto;

public class RecipeResponse {
    private String recipe;
    private String message;
    private String error;

    public RecipeResponse() {
    }

    public RecipeResponse(String recipe, String message, String error) {
        this.recipe = recipe;
        this.message = message;
        this.error = error;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
