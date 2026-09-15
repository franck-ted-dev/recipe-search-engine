package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

public interface InputReader {
    void readAndStoreLines(int numberLines, Storage storage);
    String readWord();
    int readUserChoice();
}
