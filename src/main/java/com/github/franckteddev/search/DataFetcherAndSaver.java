package com.github.franckteddev.search;

import com.github.franckteddev.search.connexion.RawDataFetcher;
import com.github.franckteddev.search.db.RecipeRepository;
import com.github.franckteddev.search.mapper.RecipeMapper;
import com.github.franckteddev.search.model.Recipe;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class DataFetcherAndSaver {
    private static final Logger LOGGER = Logger.getLogger(DataFetcherAndSaver.class.getName());

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        String url = "jdbc:postgresql://localhost:5432/recipe_search";
        String user = "recipe_user";
        String password = "recipe_password";
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            RecipeRepository recipeRepository = new RecipeRepository(connection);
            RawDataFetcher rawDataFetcher = new RawDataFetcher();
            RecipeMapper recipeMapper = new RecipeMapper();
            List<Recipe> recipes = recipeMapper.map(rawDataFetcher.fetchAll());
            for(Recipe recipe : recipes) {
                try {
                    recipeRepository.save(recipe);
                } catch (SQLException ex) {
                    LOGGER.severe("Error saving recipe: " + ex.getMessage());
                }
            }
            LOGGER.info("Data fetching and saving completed");
        }
    }
}
