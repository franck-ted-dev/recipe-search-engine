package com.github.franckteddev.search.handler;

import com.github.franckteddev.search.model.CanonicalIngredient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class CanonicalIngredientHandler implements HttpHandler, JsonResponseSender {
    private final List<CanonicalIngredient> canonicalIngredients;

    public CanonicalIngredientHandler(List<CanonicalIngredient> canonicalIngredients) {
        this.canonicalIngredients = canonicalIngredients;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        sendJsonResponse(exchange, 200, buildResponse());
    }

    private JSONObject buildResponse() {
        JSONObject response = new JSONObject();
        JSONArray ingredients = new JSONArray();
        for (CanonicalIngredient ingredient : canonicalIngredients) {
            ingredients.put(ingredient.name());
        }
        response.put("ingredients", ingredients);
        return response;
    }
}
