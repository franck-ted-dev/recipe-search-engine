package com.github.franckteddev.search;

import com.github.franckteddev.search.connexion.RawDataFetcher;
import com.github.franckteddev.search.input.RecipeDataReader;
import com.github.franckteddev.search.mapper.RecipeMapper;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.store.DynamicSizeStorage;
import com.github.franckteddev.search.store.Storage;

import java.io.IOException;

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
    }
}