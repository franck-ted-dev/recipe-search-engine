package com.github.franckteddev.search.search;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AllStrategyTest {
    @Test
    void executeStrategyReturnsTheIntersectionAllGivenSets() {
        AllStrategy strategy = new AllStrategy();
        List<Set<Integer>> list = new ArrayList<>();
        Set<Integer> set1 = Set.of(1, 2, 3);
        Set<Integer> set2 = Set.of(2, 3, 4);
        list.add(set1);
        list.add(set2);
        Set<Integer> result = strategy.executeStrategy(list, 0 );
        Set<Integer> expected = Set.of(2, 3);
        assertEquals(expected, result);
    }

    @Test
    void executeStrategyDoesNotAlterTheGivenSets() {
        AllStrategy strategy = new AllStrategy();
        List<Set<Integer>> list = new ArrayList<>();
        Set<Integer> set1 = new HashSet<>(List.of(1, 2, 3));
        Set<Integer> set2 = new HashSet<>(List.of(2, 3, 4));

        list.add(set1);
        list.add(set2);

        Set<Integer> set1Snapshot = new HashSet<>(set1);
        Set<Integer> set2Snapshot = new HashSet<>(set2);

        strategy.executeStrategy(list, 0);

        assertEquals(set1Snapshot, set1);
        assertEquals(set2Snapshot, set2);
    }
}
