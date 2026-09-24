package com.github.franckteddev.search.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;

public class UnknownPathHandler implements HttpHandler, JsonResponseSender{
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        json.put("error", "Page not found");
        sendJsonResponse(exchange, 404, json);
    }
}
