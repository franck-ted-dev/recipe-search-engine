package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

import java.io.IOException;

public interface LinesReader {
    void readAndStoreLines(Storage storage) throws IOException;
}
