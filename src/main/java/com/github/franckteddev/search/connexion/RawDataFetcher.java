package com.github.franckteddev.search.connexion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RawDataFetcher {
    private static final Logger logger = Logger.getLogger(RawDataFetcher.class.getName());

    public List<String> fetchAll() throws IOException, InterruptedException {
        List<String> results = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {
            for(char c = 'a'; c <= 'z'; c++){
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("https://www.themealdb.com/api/json/v1/1/search.php?f=" + c))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if(status != 200){
                    logger.warning("Échec pour la lettre '" + c + "' : code " + status);
                }else{
                    results.add(response.body());
                }
            }
        }
        return results;
    }
}
