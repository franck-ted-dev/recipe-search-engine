package com.github.franckteddev.search;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.franckteddev.search.utility.Normalizer.normalize;

public class CanonicalIngredientExtractor {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/recipe_search";
        String user = "recipe_user";
        String password = "recipe_password";
        Map<String, Integer> ingredientCounts = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String query = "SELECT DISTINCT name FROM ingredient";
            try(Statement statement = connection.createStatement()){
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()){
                    String ingredientLine = normalize(resultSet.getString("name"));
                    String[] ingredientParts = ingredientLine.split("\\s+");
                    for (String ingredientPart : ingredientParts) {
                        ingredientCounts.put(ingredientPart, ingredientCounts.getOrDefault(ingredientPart, 0) + 1);
                    }
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("frequences.txt"))) {
            List<Map.Entry<String, Integer>> sorted = ingredientCounts.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .toList();
            for(Map.Entry<String, Integer> entry : sorted){
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Impossible to write in the file");
        }
    }
}
