package com.github.franckteddev.search.store;

import java.util.List;

public interface Storage<T> {
    /**
     * With this method we can add an element to the storage
     *
     */
    void add(T element);
    List<T> getAll();
}
