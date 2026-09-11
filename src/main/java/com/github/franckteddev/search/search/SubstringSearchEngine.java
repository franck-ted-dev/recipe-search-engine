package com.github.franckteddev.search.search;

import com.github.franckteddev.search.store.Storage;

import java.util.ArrayList;
import java.util.List;

public class SubstringSearchEngine implements SearchEngine{
    private final Storage storage;
    public SubstringSearchEngine(Storage storage){
        this.storage = storage;
    }

    @Override
    public List<String> search(String query){
        query = query.trim();
        query = query.toLowerCase();

        List<String> results = new ArrayList<>();

        for(String line : storage.getAll()){
            String lineLowerCase = line.toLowerCase();
            if(lineLowerCase.contains(query)){
                results.add(line);
            }
        }
        return results;
    }
}
