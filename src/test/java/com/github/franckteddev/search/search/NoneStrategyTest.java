package com.github.franckteddev.search.search;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NoneStrategyTest {

    @Test
    void executeStrategyReturnsTheDifferenceBetweenTheSubsetAndTheGivenSets() {
        NoneStrategy strategy = new NoneStrategy();
        List<Set<Integer>> list = new ArrayList<>();
        Set<Integer> set1 = Set.of(1, 2, 3);
        Set<Integer> set2 = Set.of(2, 3, 4);
        list.add(set1);
        list.add(set2);
        int totalLines = 10;

        Set<Integer> result = strategy.executeStrategy(list, totalLines);

        assertEquals(Set.of(0,5,6,7,8,9), result);
    }
}
