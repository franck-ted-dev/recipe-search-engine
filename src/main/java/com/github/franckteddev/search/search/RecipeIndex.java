package com.github.franckteddev.search.search;

import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.store.Storage;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class RecipeIndex {
    private final HashMap<String, Recipe> index;

    public RecipeIndex(Storage<Recipe> storage) {
        List<Recipe> recipes = storage.getAll();
        this.index = new HashMap<>();
        for (Recipe recipe : recipes) {
            index.put(recipe.id(), recipe);
        }
    }

    public Optional<Recipe> findById(String id) {
        return Optional.ofNullable(index.get(id));
    }
}
