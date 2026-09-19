package com.github.franckteddev.search.search;

import java.util.List;
import java.util.Set;

public interface SearchStrategy {
    Set<Integer> executeStrategy(List<Set<Integer>> setsOfPositions, int totalLines);
}
