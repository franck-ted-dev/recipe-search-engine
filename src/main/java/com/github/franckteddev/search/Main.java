package com.github.franckteddev.search;

import com.github.franckteddev.search.connexion.RawDataFetcher;
import com.github.franckteddev.search.filter.MethodFilter;
import com.github.franckteddev.search.handler.HealthHandler;
import com.github.franckteddev.search.handler.RecipeHandler;
import com.github.franckteddev.search.handler.SearchHandler;
import com.github.franckteddev.search.handler.UnknownPathHandler;
import com.github.franckteddev.search.input.RecipeDataReader;
import com.github.franckteddev.search.mapper.RecipeMapper;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.search.InvertedIndexSearchEngine;
import com.github.franckteddev.search.search.RecipeIndex;
import com.github.franckteddev.search.store.DynamicSizeStorage;
import com.github.franckteddev.search.store.Storage;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        RawDataFetcher rawDataFetcher = new RawDataFetcher();
        RecipeMapper recipeMapper = new RecipeMapper();
        RecipeDataReader recipeDataReader = new RecipeDataReader(
                recipeMapper,
                rawDataFetcher.fetchAll()
        );
        Storage<Recipe> storage = new DynamicSizeStorage<>();
        recipeDataReader.readAndStoreElements(storage);
        InvertedIndexSearchEngine searchEngine = new InvertedIndexSearchEngine(storage);
        RecipeIndex recipeIndex = new RecipeIndex(storage);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.setExecutor(null);

        HttpContext healthContext = server.createContext("/health", new HealthHandler(searchEngine));
        healthContext.getFilters().add(new MethodFilter("GET"));

        HttpContext searchContext = server.createContext("/search", new SearchHandler(searchEngine));
        searchContext.getFilters().add(new MethodFilter("POST"));

        HttpContext recipeContext = server.createContext("/recipes", new RecipeHandler(recipeIndex));
        recipeContext.getFilters().add(new MethodFilter("GET"));

        server.createContext("/", new UnknownPathHandler());
        server.start();
    }
}
