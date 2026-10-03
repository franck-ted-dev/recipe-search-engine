package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.CanonicalIngredient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class CanonicalIngredientRepositoryTest {

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
    void saveInsertsCanonicalIngredientRetrievableByGetAll() throws SQLException {
        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);

        repository.save("tomato");

        List<CanonicalIngredient> ingredients = repository.getAll();

        assertEquals(1, ingredients.size());
        assertEquals("tomato", ingredients.getFirst().name());
    }

    @Test
    void saveMultipleIngredientsAreAllRetrievedByGetAll() throws SQLException {
        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);

        repository.save("tomato");
        repository.save("onion");
        repository.save("carrot");

        List<CanonicalIngredient> ingredients = repository.getAll();

        // getAll() ne trie pas ses résultats (pas de ORDER BY), donc on compare
        // par ensemble de noms plutôt que par position dans la liste.
        Set<String> names = ingredients.stream()
                .map(CanonicalIngredient::name)
                .collect(Collectors.toSet());

        assertEquals(3, ingredients.size());
        assertEquals(Set.of("tomato", "onion", "carrot"), names);
    }

    @Test
    void getAllReturnsEmptyListWhenDatabaseIsEmpty() throws SQLException {
        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);

        List<CanonicalIngredient> ingredients = repository.getAll();

        assertTrue(ingredients.isEmpty());
    }

    @Test
    void isEmptyReturnsTrueWhenNoIngredientSaved() throws SQLException {
        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);

        assertTrue(repository.isEmpty());
    }

    @Test
    void isEmptyReturnsFalseAfterSavingAnIngredient() throws SQLException {
        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(connection);

        repository.save("tomato");

        assertFalse(repository.isEmpty());
    }
}
