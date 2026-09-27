package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;

import java.sql.*;

public class RecipeRepository {
    private final Connection connection;

    public RecipeRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(Recipe recipe) throws SQLException {
        String insertRecipe = "INSERT INTO Recipe (name, instructions, country, imageURL, videoURL ) VALUES (?, ?, ?, ?, ?)";
        String insertIngredient = "INSERT INTO ingredient (name, quantity, recipe_id ) VALUES (?, ?, ?)";

        connection.setAutoCommit(false);
        try {
            int recipeId;
            try (PreparedStatement statement =
                         connection.prepareStatement(insertRecipe, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, recipe.name());
                statement.setString(2, recipe.instructions());
                statement.setString(3, recipe.country());
                statement.setString(4, recipe.imageURL());
                statement.setString(5, recipe.videoURL());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    recipeId = generatedKeys.getInt(1);
                }
            }
            try (PreparedStatement ingredientStatement = connection.prepareStatement(insertIngredient)) {
                for (Ingredient ingredient : recipe.ingredients()) {
                    ingredientStatement.setString(1, ingredient.name());
                    ingredientStatement.setString(2, ingredient.quantity());
                    ingredientStatement.setInt(3, recipeId);
                    ingredientStatement.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}
