package com.github.franckteddev.search.search;

import com.github.franckteddev.search.model.IngredientCompleted;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.model.RecipeCompleted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvertedIndexSearchEngineTest {

    private InvertedIndexSearchEngine searchEngine;

    @BeforeEach
    void setUp() {
        RecipeCompleted tomatoPasta = new RecipeCompleted(
                "0",
                "Tomato Pasta",
                "Cook the pasta, mix with tomato and olive oil.",
                "Italy",
                List.of(
                        new IngredientCompleted("tomato", "2", "tomato"),
                        new IngredientCompleted("pasta", "200g", "pasta"),
                        new IngredientCompleted("olive oil", "2 tbsp", "olive oil")
                ),
                "",
                ""
        );

        RecipeCompleted chickenTomatoSoup = new RecipeCompleted(
                "1",
                "Chicken Tomato Soup",
                "Cook chicken with tomatoes and onion.",
                "France",
                List.of(
                        new IngredientCompleted("chicken breast", "300g", "chicken"),
                        new IngredientCompleted("chopped tomatoes", "400g", "tomato"),
                        new IngredientCompleted("onion", "1", "onion")
                ),
                "",
                ""
        );

        RecipeCompleted beefTomatoStew = new RecipeCompleted(
                "2",
                "Beef Tomato Stew",
                "Simmer beef with tomatoes, onion and carrot.",
                "Belgium",
                List.of(
                        new IngredientCompleted("beef chunks", "500g", "beef"),
                        new IngredientCompleted("tomatoes", "3", "tomato"),
                        new IngredientCompleted("onion", "1", "onion"),
                        new IngredientCompleted("carrot", "2", "carrot")
                ),
                "",
                ""
        );

        RecipeCompleted fishAndChips = new RecipeCompleted(
                "3",
                "Fish and Chips",
                "Fry the fish fillet, serve with potato chips.",
                "England",
                List.of(
                        new IngredientCompleted("fish fillet", "2", "fish"),
                        new IngredientCompleted("potato", "500g", "potato")
                ),
                "",
                ""
        );

        searchEngine = new InvertedIndexSearchEngine(
                List.of(tomatoPasta, chickenTomatoSoup, beefTomatoStew, fishAndChips)
        );
    }

    @Test
    void searchWithAllThreeListsFilled() {
        SearchRequestParam query = new SearchRequestParam(
                List.of("tomato"),
                List.of("beef", "chicken"),
                List.of("fish")
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(2, results.size());
        assertEquals("Beef Tomato Stew", results.get(0).name());
        assertEquals("Chicken Tomato Soup", results.get(1).name());
    }

    @Test
    void searchWithOnlyAllFilled() {
        SearchRequestParam query = new SearchRequestParam(
                List.of("tomato"),
                List.of(),
                List.of()
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(3, results.size());
        assertEquals("Beef Tomato Stew", results.get(0).name());
        assertEquals("Chicken Tomato Soup", results.get(1).name());
        assertEquals("Tomato Pasta", results.get(2).name());
    }

    @Test
    void searchWithOnlyAnyFilled() {
        SearchRequestParam query = new SearchRequestParam(
                List.of(),
                List.of("fish", "pasta"),
                List.of()
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(2, results.size());
        assertEquals("Fish and Chips", results.get(0).name());
        assertEquals("Tomato Pasta", results.get(1).name());
    }

    @Test
    void searchWithOnlyNoneFilled() {
        SearchRequestParam query = new SearchRequestParam(
                List.of(),
                List.of(),
                List.of("onion")
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(2, results.size());
        assertEquals("Fish and Chips", results.get(0).name());
        assertEquals("Tomato Pasta", results.get(1).name());
    }

    @Test
    void searchWithAllThreeListsEmpty() {
        SearchRequestParam query = new SearchRequestParam(List.of(), List.of(), List.of());

        List<Recipe> results = searchEngine.search(query);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithMultipleTermsInAllList() {
        SearchRequestParam query = new SearchRequestParam(
                List.of("tomato", "onion"),
                List.of(),
                List.of()
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(2, results.size());
        assertEquals("Beef Tomato Stew", results.get(0).name());
        assertEquals("Chicken Tomato Soup", results.get(1).name());
    }

    @Test
    void searchWithMultipleTermsInNoneList() {
        SearchRequestParam query = new SearchRequestParam(
                List.of(),
                List.of(),
                List.of("onion", "fish")
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(1, results.size());
        assertEquals("Tomato Pasta", results.getFirst().name());
    }

    @Test
    void searchWithAllTermNotPresentInAnyRecipe() {
        SearchRequestParam query = new SearchRequestParam(
                List.of("pepper"),
                List.of(),
                List.of()
        );

        List<Recipe> results = searchEngine.search(query);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithNoneTermNotPresentInAnyRecipe() {
        SearchRequestParam query = new SearchRequestParam(
                List.of(),
                List.of(),
                List.of("pepper")
        );

        List<Recipe> results = searchEngine.search(query);

        assertEquals(4, results.size());
        assertEquals("Beef Tomato Stew", results.get(0).name());
        assertEquals("Chicken Tomato Soup", results.get(1).name());
        assertEquals("Fish and Chips", results.get(2).name());
        assertEquals("Tomato Pasta", results.get(3).name());
    }

    @Test
    void searchWithContradictingAllAndNone() {
        SearchRequestParam query = new SearchRequestParam(
                List.of("tomato"),
                List.of(),
                List.of("tomato")
        );

        List<Recipe> results = searchEngine.search(query);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchIgnoresIngredientWithNullCanonicalNameWithoutError() {
        RecipeCompleted mysteryDish = new RecipeCompleted(
                "4",
                "Mystery Dish",
                "A dish with an unmatched raw ingredient.",
                "Unknown",
                List.of(
                        new IngredientCompleted("mystery powder", "1 pinch", null),
                        new IngredientCompleted("salt", "1 tsp", "salt")
                ),
                "",
                ""
        );

        InvertedIndexSearchEngine engineWithNullCanonicalName =
                new InvertedIndexSearchEngine(List.of(mysteryDish));

        SearchRequestParam query = new SearchRequestParam(
                List.of("salt"),
                List.of(),
                List.of()
        );

        List<Recipe> results = engineWithNullCanonicalName.search(query);

        assertEquals(1, results.size());
        assertEquals("Mystery Dish", results.getFirst().name());
    }
}
