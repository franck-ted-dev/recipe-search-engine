package com.github.franckteddev.search.store;

import java.util.ArrayList;
import java.util.List;

public class DynamicSizeStorage<T> implements Storage<T>{
    private final List<T> list;

    public DynamicSizeStorage() {
        this.list = new ArrayList<>();
    }

    @Override
    public void add(T element) {
        list.add(element);
    }

    @Override
    public List<T> getAll() {
        return List.copyOf(list);
    }
}
