package com.github.franckteddev.search.utility;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.IngredientCompleted;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.model.RecipeCompleted;

import java.util.List;

public class Converter {
    private Converter(){}

    public static Recipe convertToRecipe(RecipeCompleted recipeCompleted) {
        String id = recipeCompleted.id();
        String name = recipeCompleted.name();
        String instructions = recipeCompleted.instructions();
        String country = recipeCompleted.country();
        String imageURL = recipeCompleted.imageURL();
        String videoURL = recipeCompleted.videoURL();
        List<Ingredient> ingredients = recipeCompleted.ingredientsCompleted().stream()
                .map(Converter::convertToIngredient)
                .toList();
        return new Recipe(
                id,
                name,
                instructions,
                country,
                ingredients,
                imageURL,
                videoURL
        );
    }

    public static Ingredient convertToIngredient(IngredientCompleted ingredientCompleted) {
        return new Ingredient(
                ingredientCompleted.name(),
                ingredientCompleted.quantity()
        );
    }

    public static List<Recipe> convertToRecipes(List<RecipeCompleted> recipesCompleted) {
        return recipesCompleted.stream()
                .map(Converter::convertToRecipe)
                .toList();
    }
}
