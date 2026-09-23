package com.github.franckteddev.search.model;

import java.util.List;

public record Recipe(
                     String name,
                     String instructions,
                     String country,
                     List<Ingredient> ingredients,
                     String imageURL,
                     String videoURL) {
    public Recipe {
        ingredients = List.copyOf(ingredients);
    }
}
