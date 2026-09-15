package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

import java.util.Scanner;

public class KeyboardInputReader implements InputReader{
    private final Scanner scanner;

    public KeyboardInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String readWord(){
        return scanner.nextLine();
    }

    @Override
    public int readUserChoice(){
        return Integer.parseInt(scanner.nextLine());
    }
}
