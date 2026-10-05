package com.github.franckteddev.search;

import com.github.franckteddev.search.connexion.RawDataFetcher;
import com.github.franckteddev.search.db.CanonicalIngredientRepository;
import com.github.franckteddev.search.db.RecipeRepository;
import com.github.franckteddev.search.fetchandsave.CanonicalIngredientFetcherAndSaver;
import com.github.franckteddev.search.fetchandsave.RecipeFetcherAndSaver;
import com.github.franckteddev.search.filter.CorsFilter;
import com.github.franckteddev.search.filter.MethodFilter;
import com.github.franckteddev.search.handler.*;
import com.github.franckteddev.search.model.CanonicalIngredient;
import com.github.franckteddev.search.model.RecipeCompleted;
import com.github.franckteddev.search.search.InvertedIndexSearchEngine;
import com.github.franckteddev.search.search.RecipeIndex;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import static com.github.franckteddev.search.utility.Converter.convertToRecipes;

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
        List<RecipeCompleted> recipesCompleted;
        List<CanonicalIngredient>  canonicalIngredients;

        String localUrl = "jdbc:postgresql://localhost:5432/recipe_search";
        String localUser = "recipe_user";
        String localPassword = "recipe_password";
        String url = getEnvironmentVariable(
                "DATABASE_URL",
                localUrl
        );
        String user = getEnvironmentVariable(
                "DATABASE_USER",
                localUser
        );
        String password = getEnvironmentVariable(
                "DATABASE_PASSWORD",
                localPassword
        );

        try(Connection connection = DriverManager.getConnection(url, user, password)){
            CanonicalIngredientRepository canonicalIngredientRepository =
                    new CanonicalIngredientRepository(connection);
            if(canonicalIngredientRepository.isEmpty()){
                LOGGER.info("No canonical ingredients found, fetching and saving...");
                String filename = "/ingredients_canonical.txt";
                CanonicalIngredientFetcherAndSaver canonicalIngredientFetcherAndSaver =
                        new CanonicalIngredientFetcherAndSaver(connection, filename);
                canonicalIngredientFetcherAndSaver.execute();
                LOGGER.info("Canonical ingredients fetched and saved.");
            }else{
                LOGGER.info("Canonical ingredients already present in the database.");
            }

            canonicalIngredients = canonicalIngredientRepository.getAll();
            RecipeRepository recipeRepository = new RecipeRepository(connection, canonicalIngredients);
            if(recipeRepository.isEmpty()){
                LOGGER.info("No recipes found, fetching and saving...");
                RawDataFetcher rawDataFetcher = new RawDataFetcher(
                        "https://www.themealdb.com/api/json/v1/1/search.php?f="
                );
                RecipeFetcherAndSaver recipeFetcherAndSaver = new RecipeFetcherAndSaver(
                        connection,
                        canonicalIngredients,
                        rawDataFetcher);
                recipeFetcherAndSaver.execute();
                LOGGER.info("Recipes fetched and saved.");
            }else{
                LOGGER.info("Recipes already present in the database.");
            }

            recipesCompleted = recipeRepository.getAll();
        }

        InvertedIndexSearchEngine searchEngine = new InvertedIndexSearchEngine(recipesCompleted);
        RecipeIndex recipeIndex = new RecipeIndex(convertToRecipes(recipesCompleted));

        int port = Integer.parseInt(getEnvironmentVariable(
                "PORT",
                "8000")
        );
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(null);

        String authorizedOrigin = getEnvironmentVariable(
                "CORS_ORIGIN",
                "http://localhost:63342");
        CorsFilter corsFilter = new CorsFilter(authorizedOrigin);
        MethodFilter getMethodFilter = new MethodFilter("GET");
        MethodFilter postMethodFilter = new MethodFilter("POST");

        HttpContext healthContext = server.createContext("/health", new HealthHandler(searchEngine));
        healthContext.getFilters().add(corsFilter);
        healthContext.getFilters().add(getMethodFilter);

        HttpContext ingredientContext = server.createContext(
                "/canonicalingredients",
                new CanonicalIngredientHandler(canonicalIngredients)
        );
        ingredientContext.getFilters().add(corsFilter);
        ingredientContext.getFilters().add(getMethodFilter);

        HttpContext searchContext = server.createContext("/search", new SearchHandler(searchEngine));
        searchContext.getFilters().add(corsFilter);
        searchContext.getFilters().add(postMethodFilter);

        HttpContext recipeContext = server.createContext("/recipes", new RecipeHandler(recipeIndex));
        recipeContext.getFilters().add(corsFilter);
        recipeContext.getFilters().add(getMethodFilter);

        server.createContext("/", new UnknownPathHandler());
        server.start();
    }

    private static String getEnvironmentVariable(String env, String defaultValue){
        if(!System.getenv().containsKey(env)){
            LOGGER.warning(env + " is not set, using default value.");
            return defaultValue;
        }else {
            return System.getenv(env);
        }
    }
}
