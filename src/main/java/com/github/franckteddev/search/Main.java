package com.github.franckteddev.search;

import com.github.franckteddev.search.db.CanonicalIngredientRepository;
import com.github.franckteddev.search.db.RecipeRepository;
import com.github.franckteddev.search.fetchandsave.CanonicalIngredientFetcherAndSaver;
import com.github.franckteddev.search.fetchandsave.RecipeFetcherAndSaver;
import com.github.franckteddev.search.filter.CorsFilter;
import com.github.franckteddev.search.filter.MethodFilter;
import com.github.franckteddev.search.handler.HealthHandler;
import com.github.franckteddev.search.handler.RecipeHandler;
import com.github.franckteddev.search.handler.SearchHandler;
import com.github.franckteddev.search.handler.UnknownPathHandler;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.search.InvertedIndexSearchEngine;
import com.github.franckteddev.search.search.RecipeIndex;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) throws IOException, SQLException {
        List<Recipe> recipes;

        String url = "jdbc:postgresql://localhost:5432/recipe_search";
        String user = "recipe_user";
        String password = "recipe_password";

        try(Connection connection = DriverManager.getConnection(url, user, password)){
            CanonicalIngredientRepository canonicalIngredientRepository =
                    new CanonicalIngredientRepository(connection);
            if(canonicalIngredientRepository.isEmpty()){
                LOGGER.info("No canonical ingredients found, fetching and saving...");
                CanonicalIngredientFetcherAndSaver canonicalIngredientFetcherAndSaver =
                        new CanonicalIngredientFetcherAndSaver(connection);
                canonicalIngredientFetcherAndSaver.execute();
                LOGGER.info("Canonical ingredients fetched and saved.");
            }else{
                LOGGER.info("Canonical ingredients already present in the database.");
            }

            RecipeRepository recipeRepository = new RecipeRepository(connection);
            if(recipeRepository.isEmpty()){
                LOGGER.info("No recipes found, fetching and saving...");
                RecipeFetcherAndSaver recipeFetcherAndSaver = new RecipeFetcherAndSaver(connection);
                recipeFetcherAndSaver.execute();
                LOGGER.info("Recipes fetched and saved.");
            }else{
                LOGGER.info("Recipes already present in the database.");
            }

            recipes = recipeRepository.getAll();
        }

        InvertedIndexSearchEngine searchEngine = new InvertedIndexSearchEngine(recipes);
        RecipeIndex recipeIndex = new RecipeIndex(recipes);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.setExecutor(null);

        HttpContext healthContext = server.createContext("/health", new HealthHandler(searchEngine));
        healthContext.getFilters().add(new MethodFilter("GET"));

        HttpContext searchContext = server.createContext("/search", new SearchHandler(searchEngine));
        searchContext.getFilters().add(new CorsFilter());
        searchContext.getFilters().add(new MethodFilter("POST"));

        HttpContext recipeContext = server.createContext("/recipes", new RecipeHandler(recipeIndex));
        recipeContext.getFilters().add(new MethodFilter("GET"));

        server.createContext("/", new UnknownPathHandler());
        server.start();
    }
}
