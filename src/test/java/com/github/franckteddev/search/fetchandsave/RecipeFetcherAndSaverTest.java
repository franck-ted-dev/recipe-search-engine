package com.github.franckteddev.search.fetchandsave;

import com.github.franckteddev.search.connexion.RawDataFetcher;
import com.github.franckteddev.search.db.RecipeRepository;
import com.github.franckteddev.search.model.CanonicalIngredient;
import com.github.franckteddev.search.model.RecipeCompleted;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@WireMockTest
class RecipeFetcherAndSaverTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withInitScript("schema.sql");

    private static Connection connection;

    @BeforeAll
    static void openConnection() throws SQLException {
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
    }

    @AfterAll
    static void closeConnection() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE ingredient, recipe, canonicalingredient RESTART IDENTITY CASCADE");
        }
    }

    private CanonicalIngredient insertCanonicalIngredient(String name) throws SQLException {
        String sql = "INSERT INTO CanonicalIngredient (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                return new CanonicalIngredient(generatedKeys.getInt(1), name);
            }
        }
    }

    private String baseUrl(WireMockRuntimeInfo wireMockRuntimeInfo) {
        return wireMockRuntimeInfo.getHttpBaseUrl() + "/api/json/v1/1/search.php?f=";
    }

    @Test
    void executeSavesValidRecipeFetchedFromApiWithCanonicalMatch(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, SQLException {
        CanonicalIngredient tomato = insertCanonicalIngredient("tomato");

        stubFor(get(urlEqualTo("/api/json/v1/1/search.php?f=t")).willReturn(okJson("""
                {"meals":[{"idMeal":"1","strMeal":"Tomato Soup","strInstructions":"Cook it.",
                "strCountry":"France","strMealThumb":"","strYoutube":"",
                "strIngredient1":"tomato","strMeasure1":"2"}]}
                """)));

        RawDataFetcher rawDataFetcher = new RawDataFetcher(baseUrl(wireMockRuntimeInfo));
        RecipeFetcherAndSaver fetcherAndSaver =
                new RecipeFetcherAndSaver(connection, List.of(tomato), rawDataFetcher);

        fetcherAndSaver.execute();

        List<RecipeCompleted> recipesCompleted = new RecipeRepository(connection, List.of()).getAll();

        assertEquals(1, recipesCompleted.size());
        RecipeCompleted saved = recipesCompleted.getFirst();
        assertEquals("Tomato Soup", saved.name());
        assertEquals(1, saved.ingredientsCompleted().size());
        assertEquals("tomato", saved.ingredientsCompleted().getFirst().canonicalName());
    }

    @Test
    void executeSkipsInvalidMealsFromApiResponse(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, SQLException {
        stubFor(get(urlEqualTo("/api/json/v1/1/search.php?f=t")).willReturn(okJson("""
                {"meals":[
                  {"idMeal":"1","strMeal":"Tomato Soup","strInstructions":"Cook it.",
                  "strCountry":"France","strMealThumb":"","strYoutube":"",
                  "strIngredient1":"tomato","strMeasure1":"2"},
                  {"idMeal":"2","strMeal":"Broken Recipe","strInstructions":"",
                  "strCountry":"France","strMealThumb":"","strYoutube":"",
                  "strIngredient1":"onion","strMeasure1":"1"}
                ]}
                """)));

        RawDataFetcher rawDataFetcher = new RawDataFetcher(baseUrl(wireMockRuntimeInfo));
        RecipeFetcherAndSaver fetcherAndSaver =
                new RecipeFetcherAndSaver(connection, List.of(), rawDataFetcher);

        fetcherAndSaver.execute();

        List<RecipeCompleted> recipesCompleted = new RecipeRepository(connection, List.of()).getAll();

        // "Broken Recipe" n'a pas d'instructions : RecipeMapper doit l'ignorer,
        // sans empêcher "Tomato Soup" d'être sauvegardée.
        assertEquals(1, recipesCompleted.size());
        assertEquals("Tomato Soup", recipesCompleted.getFirst().name());
    }

    @Test
    void executeSavesNothingWhenNoRecipesAreFetched(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, SQLException {
        // Aucun stub configuré : toutes les lettres répondent 404, fetchAll() ne renvoie rien.
        RawDataFetcher rawDataFetcher = new RawDataFetcher(baseUrl(wireMockRuntimeInfo));
        RecipeFetcherAndSaver fetcherAndSaver =
                new RecipeFetcherAndSaver(connection, List.of(), rawDataFetcher);

        fetcherAndSaver.execute();

        List<RecipeCompleted> recipesCompleted = new RecipeRepository(connection, List.of()).getAll();

        assertTrue(recipesCompleted.isEmpty());
    }

    @Test
    void executeContinuesSavingRemainingRecipesWhenOneFailsToSave(WireMockRuntimeInfo wireMockRuntimeInfo)
            throws IOException, SQLException {
        // Ingrédient canonique fabriqué avec un id inexistant en base : toute recette
        // qui s'y associe violera la contrainte de clé étrangère au moment de save().
        CanonicalIngredient badCanonicalIngredient = new CanonicalIngredient(99999, "badingredient");

        stubFor(get(urlEqualTo("/api/json/v1/1/search.php?f=t")).willReturn(okJson("""
                {"meals":[
                  {"idMeal":"1","strMeal":"Bad Recipe","strInstructions":"Will fail.",
                  "strCountry":"","strMealThumb":"","strYoutube":"",
                  "strIngredient1":"badingredient","strMeasure1":"1"},
                  {"idMeal":"2","strMeal":"Good Recipe","strInstructions":"Will succeed.",
                  "strCountry":"","strMealThumb":"","strYoutube":"",
                  "strIngredient1":"normalstuff","strMeasure1":"1"}
                ]}
                """)));

        RawDataFetcher rawDataFetcher = new RawDataFetcher(baseUrl(wireMockRuntimeInfo));
        RecipeFetcherAndSaver fetcherAndSaver =
                new RecipeFetcherAndSaver(connection, List.of(badCanonicalIngredient), rawDataFetcher);

        fetcherAndSaver.execute();

        List<RecipeCompleted> recipesCompleted = new RecipeRepository(connection, List.of()).getAll();

        // "Bad Recipe" échoue entièrement (transaction annulée par le rollback dans save()),
        // mais "Good Recipe" doit quand même être sauvegardée : une erreur sur une recette
        // ne doit pas interrompre le traitement des suivantes.
        assertEquals(1, recipesCompleted.size());
        assertEquals("Good Recipe", recipesCompleted.getFirst().name());
    }
}
