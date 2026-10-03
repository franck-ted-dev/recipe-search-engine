package com.github.franckteddev.search.fetchandsave;

import com.github.franckteddev.search.db.CanonicalIngredientRepository;
import com.github.franckteddev.search.model.CanonicalIngredient;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class CanonicalIngredientFetcherAndSaverTest {

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

    @Test
    void executeSavesNormalizedNonBlankCanonicalIngredientsFromFile() throws IOException, SQLException {
        // Le fichier de test contient : "tomato:vegetable", "  Onion :vegetable" (casse et
        // espaces volontairement mal formés, pour vérifier que normalize() est bien appliqué),
        // une ligne vide (doit être ignorée), et "chicken:meat".
        CanonicalIngredientFetcherAndSaver fetcherAndSaver =
                new CanonicalIngredientFetcherAndSaver(connection, "/ingredients_canonical_test.txt");

        fetcherAndSaver.execute();

        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);
        List<CanonicalIngredient> ingredients = repository.getAll();

        Set<String> names = ingredients.stream()
                .map(CanonicalIngredient::name)
                .collect(Collectors.toSet());

        assertEquals(3, ingredients.size());
        assertEquals(Set.of("tomato", "onion", "chicken"), names);
    }

    @Test
    void executeThrowsIOExceptionWhenFileIsMissingFromClasspath() throws SQLException {
        CanonicalIngredientFetcherAndSaver fetcherAndSaver =
                new CanonicalIngredientFetcherAndSaver(connection, "/does_not_exist.txt");

        assertThrows(IOException.class, fetcherAndSaver::execute);

        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);
        assertTrue(repository.isEmpty());
    }
}
