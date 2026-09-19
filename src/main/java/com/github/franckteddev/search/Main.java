package com.github.franckteddev.search;

import com.github.franckteddev.search.input.FileLinesReader;
import com.github.franckteddev.search.input.InputReader;
import com.github.franckteddev.search.input.KeyboardInputReader;
import com.github.franckteddev.search.input.LinesReader;
import com.github.franckteddev.search.output.ConsoleOutput;
import com.github.franckteddev.search.output.ResponsePresenter;
import com.github.franckteddev.search.search.InvertedIndexSearchEngine;
import com.github.franckteddev.search.store.DynamicSizeStorage;
import com.github.franckteddev.search.store.Storage;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ResponsePresenter presenter = new ConsoleOutput();

        String dataPath = null;
        if(args.length >= 2 && args[0].equals("--data")){
            dataPath = args[1];
        }

        if(dataPath == null){
            presenter.output("Please provide a data path!");
            presenter.output("Usage: java Main --data <data_path>");
            return;
        }

        LinesReader linesReader = new FileLinesReader(dataPath);

        Storage storage = new DynamicSizeStorage();

        try {
            linesReader.readAndStoreLines(storage);
        } catch (IOException e) {
            presenter.output("Unable to read data!");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        InputReader reader = new KeyboardInputReader(scanner);
        InvertedIndexSearchEngine searcher = new InvertedIndexSearchEngine(storage);

        Menu menu = new Menu(presenter, storage, searcher, reader);
        menu.start();
    }
}
