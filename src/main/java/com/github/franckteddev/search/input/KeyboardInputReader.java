package com.github.franckteddev.search.input;

import com.github.franckteddev.search.store.Storage;

import java.util.Scanner;

public class KeyboardInputReader implements InputReader{
    private final Scanner scanner;

    public KeyboardInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public int readNumberLines() {
        return Integer.parseInt(scanner.nextLine());
    }

    @Override
    public void readAndStoreLines(int count, Storage storage) {
        for(int i = 0; i < count; i++) {
            storage.add(scanner.nextLine());
        }
    }

    @Override
    public String readWord(){
        return scanner.nextLine();
    }
}
