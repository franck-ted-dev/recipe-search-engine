package com.github.franckteddev.search.handler;

import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.search.RecipeIndex;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;

import static com.github.franckteddev.search.utility.JsonBuilder.buildJsonRecipe;

public class RecipeHandler implements HttpHandler, JsonResponseSender{
    private final RecipeIndex index;

    public RecipeHandler(RecipeIndex index) {
        this.index = index;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);
        Optional<Recipe> recipe  = index.findById(id);
        if (recipe.isPresent()) {
            sendJsonResponse(exchange, 200, buildJsonRecipe(recipe.get()));
        } else {
            sendJsonResponse(exchange, 404, buildJsonError());
        }
    }

    private JSONObject buildJsonError() {
        return new JSONObject().put("error", "Recipe not found");
    }
}
