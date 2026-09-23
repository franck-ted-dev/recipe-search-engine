package com.github.franckteddev.search.handler;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.search.InvertedIndexSearchEngine;
import com.github.franckteddev.search.search.SearchRequestParam;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchHandler implements HttpHandler, JsonResponseSender{
    private final InvertedIndexSearchEngine searchEngine;

    public SearchHandler(InvertedIndexSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SearchRequestParam requestParams = readRequestParams(readRequest(exchange));
        List<Recipe> recipes = searchEngine.search(requestParams);
        sendJsonResponse(exchange, 200, buildResponse(recipes));
    }

    private JSONObject readRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        byte[] bodyBytes = requestBody.readAllBytes();
        return new JSONObject(new String(bodyBytes, StandardCharsets.UTF_8));
    }

    private SearchRequestParam readRequestParams(JSONObject requestBody) {
        JSONArray all = requestBody.optJSONArray("all");
        JSONArray any = requestBody.optJSONArray("any");
        JSONArray none = requestBody.optJSONArray("none");
        return new SearchRequestParam(
                getSearchTerms(all),
                getSearchTerms(any),
                getSearchTerms(none)
        );
    }

    private List<String> getSearchTerms(JSONArray jsonArray) {
        List<String> terms = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                terms.add(jsonArray.getString(i));
            }
        }
        return terms;
    }

    private JSONObject buildResponse(List<Recipe> recipes) {
        JSONArray arrayRecipes = new JSONArray();
        for (Recipe recipe : recipes) {
            JSONObject objectRecipe = new JSONObject();
            objectRecipe.put("name", recipe.name());
            objectRecipe.put("ingredients", buildIngredients(recipe));
            objectRecipe.put("instructions", recipe.instructions());
            objectRecipe.put("country", recipe.country());
            objectRecipe.put("image", recipe.imageURL());
            objectRecipe.put("video", recipe.videoURL());
            arrayRecipes.put(objectRecipe);
        }
        return new JSONObject().put("recipes", arrayRecipes);
    }

    private JSONArray buildIngredients(Recipe recipe) {
        JSONArray arrayIngredients = new JSONArray();
        for (Ingredient ingredient : recipe.ingredients()) {
            JSONObject objectIngredient = new JSONObject();
            objectIngredient.put("name", ingredient.name());
            objectIngredient.put("quantity", ingredient.quantity());
            arrayIngredients.put(objectIngredient);
        }
        return arrayIngredients;
    }
}