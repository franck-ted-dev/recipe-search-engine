package com.github.franckteddev.search.store;

import java.util.List;

public interface Storage {
    /**
     * With this method we can add a line to the storage
     *
     * @throws IllegalStateException if the storage is full
     */
    void add(String line);
    List<String> getAll();
}
