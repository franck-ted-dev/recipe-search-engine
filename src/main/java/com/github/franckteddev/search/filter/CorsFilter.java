package com.github.franckteddev.search.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsFilter extends Filter {
    private final String authorizedOrigin;

    public CorsFilter(String authorizedOrigin) {
        this.authorizedOrigin = authorizedOrigin;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", authorizedOrigin);
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
        } else {
            chain.doFilter(exchange);
        }
    }

    @Override
    public String description() {
        return "CORS filter";
    }
}
