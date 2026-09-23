package com.github.franckteddev.search.input;

import com.github.franckteddev.search.mapper.RecipeMapper;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.store.Storage;

import java.util.List;

public class RecipeDataReader implements ElementReader<Recipe>{
    private final RecipeMapper recipeMapper;
    private final List<String> rawJson;

    public RecipeDataReader(RecipeMapper recipeMapper, List<String> rawJson){
        this.recipeMapper = recipeMapper;
        this.rawJson = rawJson;
    }

    @Override
    public void readAndStoreElements(Storage<Recipe> storage){
        for(Recipe recipe : recipeMapper.map(rawJson)){
            storage.add(recipe);
        }
    }
}
