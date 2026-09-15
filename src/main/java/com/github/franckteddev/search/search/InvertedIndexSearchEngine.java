package com.github.franckteddev.search.search;

import com.github.franckteddev.search.store.Storage;

import java.util.*;

public class InvertedIndexSearchEngine implements SearchEngine{
    private final Map<String, Set<Integer>> invertedIndex;
    private final List<String> allLines;

    public InvertedIndexSearchEngine(Storage storage){
        this.invertedIndex = new HashMap<>();
        this.allLines = storage.getAll();
        int pos = 0;

        for (String line : allLines){
            line = line.trim().toLowerCase();
            if (!line.isEmpty()) {
                String[] words = line.split("\\s+");
                for (String word : words){
                    invertedIndex.computeIfAbsent(word, k -> new LinkedHashSet<>()).add(pos);
                }
            }
            pos++;
        }
    }

    @Override
    public List<String> search(String query) {
        query = query.trim();
        query = query.toLowerCase();
        Set<Integer> positions = this.invertedIndex.getOrDefault(query, Collections.emptySet());
        return positions.stream().map(allLines::get).toList();
    }
}
