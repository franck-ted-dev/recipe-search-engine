package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileLinesReader implements LinesReader{
    private final String filename;

    public FileLinesReader(String filename) {
        this.filename = filename;
    }

    @Override
    public void readAndStoreLines(Storage storage) throws IOException{
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                storage.add(line);
            }
        }
    }
}
