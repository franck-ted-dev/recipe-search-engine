package com.github.franckteddev.search.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AnyStrategy implements SearchStrategy  {

    @Override
    public Set<Integer> executeStrategy(List<Set<Integer>> setsOfPositions, int totalLines) {
        Set<Integer> positions = new LinkedHashSet<>(setsOfPositions.getFirst());
        for (int i = 1; i < setsOfPositions.size(); i++) {
            positions.addAll(setsOfPositions.get(i));
        }
        return positions;
    }
}
