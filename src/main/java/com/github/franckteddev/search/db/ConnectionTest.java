package com.github.franckteddev.search.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/recipe_search";
        String user = "recipe_user";
        String password = "recipe_password";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connection reussie !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
