package com.github.franckteddev.search.fetchandsave;

import com.github.franckteddev.search.connexion.RawDataFetcher;
import com.github.franckteddev.search.db.RecipeRepository;
import com.github.franckteddev.search.mapper.RecipeMapper;
import com.github.franckteddev.search.model.CanonicalIngredient;
import com.github.franckteddev.search.model.Recipe;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class RecipeFetcherAndSaver extends FetcherAndSaver {
    private static final Logger LOGGER = Logger.getLogger(RecipeFetcherAndSaver.class.getName());
    private final List<CanonicalIngredient> canonicalIngredients;

    public RecipeFetcherAndSaver(Connection connection,
                                 List<CanonicalIngredient> canonicalIngredients) {
        super(connection);
        this.canonicalIngredients = canonicalIngredients;
    }

    @Override
    public void execute() throws IOException {
        RecipeRepository recipeRepository = new RecipeRepository(connection, canonicalIngredients);

        List<Recipe> recipes;
        RawDataFetcher rawDataFetcher = new RawDataFetcher();
        RecipeMapper recipeMapper = new RecipeMapper();
        try {
            recipes = recipeMapper.map(rawDataFetcher.fetchAll());
        } catch (InterruptedException ex) {
            LOGGER.severe("Error fetching and mapping recipes: " + ex.getMessage());
            return;
        }
        for (Recipe recipe : recipes) {
            try {
                recipeRepository.save(recipe);
            } catch (SQLException ex) {
                LOGGER.severe("Error saving recipe: " + ex.getMessage());
            }
        }
        LOGGER.info("Recipes fetching and saving completed");
    }
}
