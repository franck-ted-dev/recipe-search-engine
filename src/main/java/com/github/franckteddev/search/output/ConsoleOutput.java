package com.github.franckteddev.search.output;

import java.util.List;

public class ConsoleOutput implements ResponsePresenter{
    @Override
    public void output(List<String> results) {
        if (results.isEmpty()) {
            System.out.println("No matching results found");
            return;
        }
        System.out.println("Found:");
        results.forEach(System.out::println);
    }
}
