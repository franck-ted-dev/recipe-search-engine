package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

import java.io.IOException;

public interface ElementReader<E> {
    void readAndStoreElements(Storage<E> storage) throws IOException;
}
