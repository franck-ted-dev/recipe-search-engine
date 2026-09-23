package com.github.franckteddev.search.search;

import com.github.franckteddev.search.model.Recipe;

import java.util.List;

public interface SearchEngine {
    /**
     * Search for a query in the stored elements.
     *
     * @param query the query to search for
     * @return the list of recipes that match the query, never returns {@code null}
     */
    List<Recipe> search(SearchRequestParam query);
}
