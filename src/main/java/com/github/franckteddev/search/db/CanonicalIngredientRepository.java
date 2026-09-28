package com.github.franckteddev.search.db;

import com.github.franckteddev.search.model.CanonicalIngredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CanonicalIngredientRepository {
    private final Connection connection;

    public CanonicalIngredientRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(String ingredient) throws SQLException{
        String sql = "INSERT INTO canonicalingredient (name) VALUES (?)";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ingredient);
            statement.executeUpdate();
        }
    }

    public List<CanonicalIngredient> getAll() throws SQLException {
        List<CanonicalIngredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name FROM canonicalingredient";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                ingredients.add(
                        new CanonicalIngredient(
                                resultSet.getInt("id"),
                                resultSet.getString("name")
                        )
                );
            }
        }
        return ingredients;
    }

    public boolean isEmpty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM canonicalingredient";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1) == 0;
        }
    }
}
