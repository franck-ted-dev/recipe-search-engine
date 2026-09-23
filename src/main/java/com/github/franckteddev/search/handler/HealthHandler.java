package com.github.franckteddev.search.handler;

import com.github.franckteddev.search.search.InvertedIndexSearchEngine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;

public class HealthHandler implements HttpHandler, JsonResponseSender {
    private final InvertedIndexSearchEngine searchEngine;

    public HealthHandler(InvertedIndexSearchEngine searchEngine){
        this.searchEngine = searchEngine;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(searchEngine.isReady()){
            JSONObject json = new JSONObject().put("status", "OK");
            sendJsonResponse(exchange, 200, json);
        } else {
            JSONObject json = new JSONObject().put("status", "NOT_INITIALIZED");
            sendJsonResponse(exchange, 503, json);
        }
    }
}
