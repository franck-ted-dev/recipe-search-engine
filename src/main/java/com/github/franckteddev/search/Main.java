package com.github.franckteddev.search;

import com.github.franckteddev.search.input.InputReader;
import com.github.franckteddev.search.input.KeyboardInputReader;
import com.github.franckteddev.search.output.ConsoleOutput;
import com.github.franckteddev.search.output.ResponsePresenter;
import com.github.franckteddev.search.search.SearchEngine;
import com.github.franckteddev.search.search.SubstringSearchEngine;
import com.github.franckteddev.search.store.FixedSizeStorage;
import com.github.franckteddev.search.store.Storage;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputReader reader = new KeyboardInputReader(scanner);
        int n = reader.readNumberLines();

        Storage storage = new FixedSizeStorage(n);
        reader.readAndStoreLines(n, storage);

        SearchEngine searcher = new SubstringSearchEngine(storage);
        ResponsePresenter presenter = new ConsoleOutput();

        int m = reader.readNumberSearches();
        for (int i = 0; i < m; i++) {
            presenter.output(searcher.search(reader.readWord()));
        }
    }
}
