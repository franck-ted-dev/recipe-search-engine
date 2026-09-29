package com.github.franckteddev.search.model;

import java.util.List;

public record RecipeCompleted(
        String id,
        String name,
        String instructions,
        String country,
        List<IngredientCompleted> ingredientsCompleted,
        String imageURL,
        String videoURL) {
    public RecipeCompleted {
        ingredientsCompleted = List.copyOf(ingredientsCompleted);
    }
}
