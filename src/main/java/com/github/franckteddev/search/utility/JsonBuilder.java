package com.github.franckteddev.search.utility;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonBuilder {

    private JsonBuilder() {
    }

    public static JSONArray buildIngredients(Recipe recipe) {
        JSONArray arrayIngredients = new JSONArray();
        for (Ingredient ingredient : recipe.ingredients()) {
            JSONObject objectIngredient = new JSONObject();
            objectIngredient.put("name", ingredient.name());
            objectIngredient.put("quantity", ingredient.quantity());
            arrayIngredients.put(objectIngredient);
        }
        return arrayIngredients;
    }

    public static JSONObject buildJsonRecipe(Recipe recipe) {
        JSONObject json = new JSONObject();
        json.put("id", recipe.id());
        json.put("name", recipe.name());
        json.put("ingredients", JsonBuilder.buildIngredients(recipe));
        json.put("instructions", recipe.instructions());
        json.put("country", recipe.country());
        json.put("image", recipe.imageURL());
        json.put("video", recipe.videoURL());
        return json;
    }
}
