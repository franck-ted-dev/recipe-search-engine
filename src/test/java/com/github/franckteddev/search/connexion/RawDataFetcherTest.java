package com.github.franckteddev.search.connexion;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WireMockTest
class RawDataFetcherTest {

    @Test
    void fetchAllReturnsResponseBodyForEverySuccessfulLetter(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, InterruptedException {
        String body = "{\"meals\":[{\"idMeal\":\"1\",\"strMeal\":\"Any Dish\"}]}";
        stubFor(get(urlMatching("/api/json/v1/1/search\\.php\\?f=[a-z]"))
                .willReturn(okJson(body)));

        RawDataFetcher rawDataFetcher = new RawDataFetcher(
                wireMockRuntimeInfo.getHttpBaseUrl() + "/api/json/v1/1/search.php?f="
        );

        List<String> results = rawDataFetcher.fetchAll();

        assertEquals(26, results.size());
        assertTrue(results.stream().allMatch(body::equals));
    }

    @Test
    void fetchAllReturnsEmptyListWhenNoLetterSucceeds(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, InterruptedException {
        // Aucun stub configuré : WireMock répond 404 à toutes les requêtes par défaut.
        RawDataFetcher rawDataFetcher = new RawDataFetcher(
                wireMockRuntimeInfo.getHttpBaseUrl() + "/api/json/v1/1/search.php?f="
        );

        List<String> results = rawDataFetcher.fetchAll();

        assertTrue(results.isEmpty());
    }

    @Test
    void fetchAllIncludesOnlyTheSuccessfulLetterWhenOthersFail(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, InterruptedException {
        String body = "{\"meals\":[{\"idMeal\":\"2\",\"strMeal\":\"Chicken Dish\"}]}";
        stubFor(get(urlEqualTo("/api/json/v1/1/search.php?f=c"))
                .willReturn(okJson(body)));
        // Toutes les autres lettres restent sans stub : réponse 404 par défaut.

        RawDataFetcher rawDataFetcher = new RawDataFetcher(
                wireMockRuntimeInfo.getHttpBaseUrl() + "/api/json/v1/1/search.php?f="
        );

        List<String> results = rawDataFetcher.fetchAll();

        assertEquals(1, results.size());
        assertEquals(body, results.getFirst());
    }
}
