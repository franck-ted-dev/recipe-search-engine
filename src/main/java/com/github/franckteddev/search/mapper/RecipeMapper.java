package com.github.franckteddev.search.mapper;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RecipeMapper {
    private static final Logger logger = Logger.getLogger(RecipeMapper.class.getName());
    public List<Recipe> map(List<String> rawJson){
        List<Recipe> recipes = new ArrayList<>();
        for(String json : rawJson){
            JSONObject jsonObject = new JSONObject(json);
            JSONArray centralArray = jsonObject.optJSONArray("meals");
            if(centralArray==null){
                continue;
            }
            for(int i = 0; i < centralArray.length(); i++){
                JSONObject mealObject = centralArray.getJSONObject(i);
                String name = mealObject.optString("strMeal");
                if(name.isEmpty()){
                    logger.warning("Skipping recipe with empty name");
                    continue;
                }
                String instructions = mealObject.optString("strInstructions");
                if(instructions.isEmpty()){
                    logger.warning("Skipping recipe " + name + " with empty instructions");
                    continue;
                }
                String country = mealObject.optString("strCountry");
                String imageURL = mealObject.optString("strMealThumb");
                String videoURL = mealObject.optString("strYoutube");
                List<Ingredient> ingredients = constructIngredients(mealObject);
                if(ingredients.isEmpty()){
                    logger.warning("Skipping recipe " + name + " with empty ingredients");
                    continue;
                }
                recipes.add(new Recipe(name, instructions, country, ingredients, imageURL, videoURL));
            }
        }
        return recipes;
    }

    private List<Ingredient> constructIngredients(JSONObject mealObject){
        List<Ingredient> ingredients = new ArrayList<>();
        for(int i = 1; i <= 20; ++i){
            String key1 = "strIngredient" + i;
            String key2 = "strMeasure" + i;
            String ingredient = mealObject.optString(key1);
            String quantity = mealObject.optString(key2);
            if(ingredient.isEmpty() || quantity.isEmpty()){
                continue;
            }
            ingredients.add(new Ingredient(ingredient, quantity));
        }
        return ingredients;
    }
}