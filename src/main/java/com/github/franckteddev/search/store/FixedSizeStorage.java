package com.github.franckteddev.search.store;

import java.util.Arrays;
import java.util.List;

public class FixedSizeStorage implements Storage{
    private final int size;
    private int index;
    private final String[] lines;
    public FixedSizeStorage(int size) {
        this.size = size;
        this.index = 0;
        this.lines = new String[size];
    }

    @Override
    public void add(String line) {
        if(index >= size) {
            throw new IllegalStateException("Storage is full");
        }
        lines[index++] = line;
    }

    @Override
    public List<String> getAll() {
        String[] result = Arrays.copyOf(lines, index);
        return List.of(result);
    }
}
