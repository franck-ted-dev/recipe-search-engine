package com.github.franckteddev.search.store;

import java.util.ArrayList;
import java.util.List;

public class DynamicSizeStorage implements Storage{
    private final List<String> list;

    public DynamicSizeStorage() {
        this.list = new ArrayList<>();
    }

    @Override
    public void add(String line) {
        list.add(line);
    }

    @Override
    public List<String> getAll() {
        return List.copyOf(list);
    }
}
