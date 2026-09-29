package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.*;

import java.sql.*;
import java.util.*;

import static com.github.franckteddev.search.utility.Normalizer.normalize;

public class RecipeRepository {
    private final Connection connection;
    private final List<CanonicalIngredient> canonicalIngredients;

    public RecipeRepository(Connection connection, List<CanonicalIngredient> canonicalIngredients) {
        this.connection = connection;
        this.canonicalIngredients = canonicalIngredients;
    }

    public void save(Recipe recipe) throws SQLException {
        String insertRecipe = "INSERT INTO Recipe (name, instructions, country, imageURL, videoURL ) VALUES (?, ?, ?, ?, ?)";
        String insertIngredient = "INSERT INTO ingredient (name, quantity, canonical_ingredient_id, recipe_id ) VALUES (?, ?, ?, ?)";

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
                    int canonicalIngredientId = getCanonicalIngredientId(normalize(ingredient.name()));
                    if(canonicalIngredientId > 0){
                        ingredientStatement.setInt(3, canonicalIngredientId);
                    }else{
                        ingredientStatement.setNull(3, Types.INTEGER);
                    }
                    ingredientStatement.setInt(4, recipeId);
                    ingredientStatement.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private int getCanonicalIngredientId(String ingredient) {
        return canonicalIngredients.stream()
                .filter(canonicalIngredient -> ingredient.contains(canonicalIngredient.name()))
                .max(Comparator.comparing(ci->ci.name().length()))
                .map(CanonicalIngredient::id)
                .orElse(-1);
    }

    public List<RecipeCompleted> getAll() throws SQLException {
        List<RecipeCompleted> recipesCompleted = new ArrayList<>();

        String sql = "SELECT r.id AS recipe_id, r.name, r.instructions, r.country, r.imageURL, r.videoURL, " +
                "i.name AS ingredient_name, i.quantity, c.name AS canonical_ingredient_name " +
                "FROM recipe r " +
                "JOIN ingredient i ON r.id = i.recipe_id " +
                "LEFT JOIN canonicalingredient c on c.id = i.canonical_ingredient_id " +
                "ORDER BY r.id";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            Map<Integer, List<IngredientCompleted>> ingredientsCompletedByRecipeId = new LinkedHashMap<>();
            Map<Integer, String> recipeNamesById = new LinkedHashMap<>();
            Map<Integer, String> recipeInstructionsById = new LinkedHashMap<>();
            Map<Integer, String> recipeCountryById = new LinkedHashMap<>();
            Map<Integer, String> recipeImageUrlsById = new LinkedHashMap<>();
            Map<Integer, String> recipeVideoUrlsById = new LinkedHashMap<>();

            while (resultSet.next()){
                int recipeId = resultSet.getInt("recipe_id");

                String ingredientName = resultSet.getString("ingredient_name");
                String quantity = resultSet.getString("quantity");
                String canonicalIngredientName = resultSet.getString("canonical_ingredient_name");
                ingredientsCompletedByRecipeId
                        .computeIfAbsent(recipeId, k -> new ArrayList<>())
                        .add(new IngredientCompleted(ingredientName, quantity, canonicalIngredientName));

                recipeNamesById.putIfAbsent(recipeId, resultSet.getString("name"));
                recipeInstructionsById.putIfAbsent(recipeId, resultSet.getString("instructions"));
                recipeCountryById.putIfAbsent(recipeId, resultSet.getString("country"));
                recipeImageUrlsById.putIfAbsent(recipeId, resultSet.getString("imageURL"));
                recipeVideoUrlsById.putIfAbsent(recipeId, resultSet.getString("videoURL"));
            }

            for(Map.Entry<Integer, List<IngredientCompleted>> entry : ingredientsCompletedByRecipeId.entrySet()){
                RecipeCompleted recipeCompleted = new RecipeCompleted(
                        entry.getKey().toString(),
                        recipeNamesById.get(entry.getKey()),
                        recipeInstructionsById.get(entry.getKey()),
                        recipeCountryById.get(entry.getKey()),
                        entry.getValue(),
                        recipeImageUrlsById.get(entry.getKey()),
                        recipeVideoUrlsById.get(entry.getKey()));
                recipesCompleted.add(recipeCompleted);
            }
        }
        return recipesCompleted;
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
