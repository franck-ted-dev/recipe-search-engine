package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

public interface ElementReader<E> {
    void readAndStoreElements(Storage<E> storage);
}
