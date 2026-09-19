package com.github.franckteddev.search.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoneStrategy implements SearchStrategy {

    @Override
    public Set<Integer> executeStrategy(List<Set<Integer>> setsOfPositions, int totalLines) {
        Set<Integer> universum = IntStream.range(0, totalLines)
                .boxed()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Set<Integer> positions : setsOfPositions) {
            universum.removeAll(positions);
        }
        return universum;
    }
}
