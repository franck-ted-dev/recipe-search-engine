package com.github.franckteddev.search.search;

import com.github.franckteddev.search.store.DynamicSizeStorage;
import com.github.franckteddev.search.store.Storage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvertedIndexSearchEngineTest {

    private InvertedIndexSearchEngine buildEngine() {
        Storage storage = new DynamicSizeStorage();
        storage.add("Dwight Joseph djo@gmail.com");
        storage.add("Rene Webb webb@gmail.com");
        storage.add("Katie Jacobs");
        storage.add("Erick Harrington harrington@gmail.com");
        storage.add("Myrtle Medina");
        storage.add("Erick Burgess");
        return new InvertedIndexSearchEngine(storage);
    }

    @Test
    void searchWithAllStrategyReturnsLinesContainingEveryQueryWord() {
        InvertedIndexSearchEngine engine = buildEngine();
        engine.setSearchStrategy(new AllStrategy());

        List<String> result = engine.search("Harrington Erick");

        assertEquals(List.of("Erick Harrington harrington@gmail.com"), result);
    }

    @Test
    void searchWithAnyStrategyReturnsLinesContainingAtLeastOneQueryWord() {
        InvertedIndexSearchEngine engine = buildEngine();
        engine.setSearchStrategy(new AnyStrategy());

        List<String> result = engine.search("Erick Dwight webb@gmail.com");

        assertEquals(
                List.of(
                        "Erick Harrington harrington@gmail.com",
                        "Erick Burgess",
                        "Dwight Joseph djo@gmail.com",
                        "Rene Webb webb@gmail.com"
                ),
                result
        );
    }

    @Test
    void searchWithWordAbsentFromIndexDoesNotThrowAndIsSimplyIgnored() {
        InvertedIndexSearchEngine engine = buildEngine();
        engine.setSearchStrategy(new AnyStrategy());

        List<String> result = engine.search("Katie Erick QQQ");

        assertEquals(
                List.of(
                        "Katie Jacobs",
                        "Erick Harrington harrington@gmail.com",
                        "Erick Burgess"
                ),
                result
        );
    }
}