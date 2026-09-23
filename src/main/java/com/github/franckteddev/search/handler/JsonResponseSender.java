package com.github.franckteddev.search.handler;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public interface JsonResponseSender {
    default void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseOctets = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseOctets.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseOctets);
        os.close();
    }
}
