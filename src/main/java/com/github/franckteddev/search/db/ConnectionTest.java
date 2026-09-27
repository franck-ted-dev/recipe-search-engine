package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class ConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/recipe_search";
        String user = "recipe_user";
        String password = "recipe_password";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connection reussie !");
            Ingredient ingredient = new Ingredient("poulet", "1");
            Ingredient ingredient2 = new Ingredient("curry", "1");
            List<Ingredient> ingredients = List.of(ingredient, ingredient2);
            Recipe recipe = new Recipe(
                    "3434",
                    "Poulet au curry",
                    "aaaaaaaaaaaaa",
                    "France",
                    ingredients,
                    "fdfdfdf",
                    "dfdfdfdf"
            );
            RecipeRepository recipeRepository = new RecipeRepository(connection);
            recipeRepository.save(recipe);
            System.out.println("Recipe saved successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
