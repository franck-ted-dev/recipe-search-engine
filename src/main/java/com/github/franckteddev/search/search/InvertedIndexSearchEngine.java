package com.github.franckteddev.search.search;

import com.github.franckteddev.search.store.Storage;

import java.util.*;

public class InvertedIndexSearchEngine implements SearchEngine{
    private final Map<String, Set<Integer>> invertedIndex;
    private final List<String> allLines;
    private SearchStrategy searchStrategy;

    public InvertedIndexSearchEngine(Storage storage){
        this.searchStrategy = new AllStrategy();
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

    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    @Override
    public List<String> search(String query) {
        query = query.trim().toLowerCase();
        Set<Integer> positionsAfterStrategy = new HashSet<>();
        if(!query.isEmpty()){
            String[] words = query.split("\\s+");
            List<Set<Integer>> positionsList = new ArrayList<>();
            for(String word: words){
                Set<Integer> positions = this.invertedIndex.getOrDefault(word, Collections.emptySet());
                positionsList.add(positions);
            }
            positionsAfterStrategy = this.searchStrategy.executeStrategy(positionsList, allLines.size());
        }
        return positionsAfterStrategy.stream().map(allLines::get).toList();
    }
}
