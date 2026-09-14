package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

public interface InputReader {
    int readNumberLines();
    void readAndStoreLines(int numberLines, Storage storage);
    String readWord();
    int readUserChoice();
}
