package com.github.franckteddev.search.filter;

import com.github.franckteddev.search.handler.JsonResponseSender;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;

public class MethodFilter extends Filter implements JsonResponseSender {
    private final String httpMethod;

    public MethodFilter(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        if(exchange.getRequestMethod().equals(httpMethod))
        {
            chain.doFilter(exchange);
        } else {
            JSONObject json = new JSONObject();
            json.put("error", "Method not allowed");
            sendJsonResponse(exchange, 405, json);
        }
    }

    @Override
    public String description() {
        return "filter for " + httpMethod + " method";
    }
}
