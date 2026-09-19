package com.github.franckteddev.search;

import com.github.franckteddev.search.input.InputReader;
import com.github.franckteddev.search.output.ResponsePresenter;
import com.github.franckteddev.search.search.*;
import com.github.franckteddev.search.store.Storage;

public class Menu {
    private final ResponsePresenter responsePresenter;
    private final Storage storage;
    private final InvertedIndexSearchEngine searchEngine;
    private final InputReader inputReader;
    private boolean running;

    public Menu(ResponsePresenter responsePresenter,
                Storage storage,
                InvertedIndexSearchEngine searchEngine,
                InputReader inputReader) {
        this.responsePresenter = responsePresenter;
        this.storage = storage;
        this.searchEngine = searchEngine;
        this.inputReader = inputReader;
        running = true;
    }

    public void start() {
        while (running) {
            displayMainMenu();
            int userChoice = inputReader.readUserChoice();
            handleUserChoice(userChoice);
        }
    }

    private void handleUserChoice(int userChoice) {
        if(!userChoiceIsValid(userChoice)) {
            handleInvalidUserChoice();
            return;
        }

        switch(userChoice){
            case 1:
                searchInformation();
                break;
            case 2:
                printAllData();
                break;
            case 0:
                exit();
                break;
            default:
                throw new IllegalStateException("Unexpected user choice: " + userChoice);
        }
    }

    private boolean userChoiceIsValid(int userChoice){
        return userChoice >= 0 && userChoice <= 2;
    }

    private void displayMainMenu(){
        this.responsePresenter.output("=== Menu ===");
        this.responsePresenter.output("1. Search information.");
        this.responsePresenter.output("2. Print all data.");
        this.responsePresenter.output("0. Exit.");
    }

    private void searchInformation(){
        this.responsePresenter.output("Enter a word to find all suitable lines.");
        String searchQuery = inputReader.readWord();
        this.responsePresenter.output(this.searchEngine.search(searchQuery));
    }

    private void printAllData(){
        this.responsePresenter.output("=== List of lines ===");
        this.storage.getAll().forEach(this.responsePresenter::output);
    }

    private void handleInvalidUserChoice(){
        this.responsePresenter.output("Incorrect option! Try again.");
    }

    private void exit(){
        this.responsePresenter.output("Bye!");
        running = false;
    }
}
