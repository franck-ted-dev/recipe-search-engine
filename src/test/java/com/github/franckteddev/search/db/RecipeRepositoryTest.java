package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.CanonicalIngredient;
import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.model.RecipeCompleted;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class RecipeRepositoryTest {

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

    @Test
    void saveInsertsRecipeAndMatchesExactCanonicalIngredient() throws SQLException {
        CanonicalIngredient tomato = insertCanonicalIngredient("tomato");
        RecipeRepository repository = new RecipeRepository(connection, List.of(tomato));

        Recipe recipe = new Recipe(
                "unused",
                "Tomato Soup",
                "Cook the tomatoes.",
                "France",
                List.of(new Ingredient("tomato", "2")),
                "",
                ""
        );

        repository.save(recipe);

        List<RecipeCompleted> recipesCompleted = repository.getAll();

        assertEquals(1, recipesCompleted.size());
        RecipeCompleted saved = recipesCompleted.getFirst();
        assertEquals("Tomato Soup", saved.name());
        assertEquals(1, saved.ingredientsCompleted().size());
        assertEquals("tomato", saved.ingredientsCompleted().getFirst().canonicalName());
    }

    @Test
    void saveChoosesLongestMatchingCanonicalIngredientOnAmbiguity() throws SQLException {
        CanonicalIngredient chicken = insertCanonicalIngredient("chicken");
        CanonicalIngredient chickenBreast = insertCanonicalIngredient("chicken breast");
        RecipeRepository repository = new RecipeRepository(connection, List.of(chicken, chickenBreast));

        Recipe recipe = new Recipe(
                "unused",
                "Chicken Dish",
                "Cook the chicken breast.",
                "France",
                List.of(new Ingredient("chicken breast", "300g")),
                "",
                ""
        );

        repository.save(recipe);

        RecipeCompleted saved = repository.getAll().getFirst();

        assertEquals("chicken breast", saved.ingredientsCompleted().getFirst().canonicalName());
    }

    @Test
    void saveSetsNullCanonicalIngredientIdWhenNoCanonicalMatches() throws SQLException {
        CanonicalIngredient onion = insertCanonicalIngredient("onion");
        RecipeRepository repository = new RecipeRepository(connection, List.of(onion));

        Recipe recipe = new Recipe(
                "unused",
                "Mystery Dish",
                "A dish with an unknown ingredient.",
                "Unknown",
                List.of(new Ingredient("durian", "1")),
                "",
                ""
        );

        repository.save(recipe);

        RecipeCompleted saved = repository.getAll().getFirst();

        assertEquals(1, saved.ingredientsCompleted().size());
        assertNull(saved.ingredientsCompleted().getFirst().canonicalName());
    }

    @Test
    void getAllGroupsMultipleIngredientsUnderSameRecipe() throws SQLException {
        CanonicalIngredient tomato = insertCanonicalIngredient("tomato");
        CanonicalIngredient onion = insertCanonicalIngredient("onion");
        RecipeRepository repository = new RecipeRepository(connection, List.of(tomato, onion));

        Recipe recipe = new Recipe(
                "unused",
                "Tomato Onion Soup",
                "Cook everything together.",
                "France",
                List.of(
                        new Ingredient("tomato", "2"),
                        new Ingredient("onion", "1"),
                        new Ingredient("salt", "1 pinch")
                ),
                "",
                ""
        );

        repository.save(recipe);

        List<RecipeCompleted> recipesCompleted = repository.getAll();

        assertEquals(1, recipesCompleted.size());
        assertEquals(3, recipesCompleted.getFirst().ingredientsCompleted().size());
    }

    @Test
    void getAllReturnsEmptyListWhenDatabaseIsEmpty() throws SQLException {
        RecipeRepository repository = new RecipeRepository(connection, List.of());

        List<RecipeCompleted> recipesCompleted = repository.getAll();

        assertTrue(recipesCompleted.isEmpty());
    }

    @Test
    void isEmptyReturnsTrueWhenNoRecipeSaved() throws SQLException {
        RecipeRepository repository = new RecipeRepository(connection, List.of());

        assertTrue(repository.isEmpty());
    }

    @Test
    void isEmptyReturnsFalseAfterSavingARecipe() throws SQLException {
        RecipeRepository repository = new RecipeRepository(connection, List.of());

        Recipe recipe = new Recipe(
                "unused",
                "Any Dish",
                "Instructions.",
                "France",
                List.of(new Ingredient("water", "1L")),
                "",
                ""
        );
        repository.save(recipe);

        assertFalse(repository.isEmpty());
    }
}
