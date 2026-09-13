package com.github.franckteddev.search.search;

import java.util.List;

public interface SearchEngine {
    /**
     * Search for a query in the stored elements.
     *
     * @param query the query to search for
     * @return the list of elements that match the query, never returns {@code null}
     */
    List<String> search(String query);
}
