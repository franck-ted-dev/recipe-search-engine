package com.github.franckteddev.search;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        String line = scanner.nextLine();
        System.out.print("> ");
        String searched = scanner.nextLine();
        String regex = "\\b" + Pattern.quote(searched) + "\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        System.out.println(matcher.find() ? "Found" : "Not found");
    }
}
