package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;

import java.sql.*;
import java.util.*;

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

    public List<Recipe> getAll() throws SQLException {
        List<Recipe> recipes = new ArrayList<>();

        String sql = "SELECT r.id AS recipe_id, r.name, r.instructions, r.country, r.imageURL, r.videoURL, " +
                "i.name AS ingredient_name, i.quantity " +
                "FROM recipe r " +
                "JOIN ingredient i ON r.id = i.recipe_id " +
                "ORDER BY r.id";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            Map<Integer, List<Ingredient>> ingredientsByRecipeId = new LinkedHashMap<>();
            Map<Integer, String> namesById = new LinkedHashMap<>();
            Map<Integer, String> instructionsById = new LinkedHashMap<>();
            Map<Integer, String> countryById = new LinkedHashMap<>();
            Map<Integer, String> imageUrlsById = new LinkedHashMap<>();
            Map<Integer, String> videoUrlsById = new LinkedHashMap<>();

            while (resultSet.next()){
                int recipeId = resultSet.getInt("recipe_id");
                String ingredientName = resultSet.getString("ingredient_name");
                String quantity = resultSet.getString("quantity");
                ingredientsByRecipeId
                        .computeIfAbsent(recipeId, k -> new ArrayList<>())
                        .add(new Ingredient(ingredientName, quantity));
                namesById.putIfAbsent(recipeId, resultSet.getString("name"));
                instructionsById.putIfAbsent(recipeId, resultSet.getString("instructions"));
                countryById.putIfAbsent(recipeId, resultSet.getString("country"));
                imageUrlsById.putIfAbsent(recipeId, resultSet.getString("imageURL"));
                videoUrlsById.putIfAbsent(recipeId, resultSet.getString("videoURL"));
            }

            for(Map.Entry<Integer, List<Ingredient>> entry : ingredientsByRecipeId.entrySet()){
                Recipe recipe = new Recipe(
                        entry.getKey().toString(),
                        namesById.get(entry.getKey()),
                        instructionsById.get(entry.getKey()),
                        countryById.get(entry.getKey()),
                        entry.getValue(),
                        imageUrlsById.get(entry.getKey()),
                        videoUrlsById.get(entry.getKey()));
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public boolean isEmpty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM recipe";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1) == 0;
        }
    }
}
