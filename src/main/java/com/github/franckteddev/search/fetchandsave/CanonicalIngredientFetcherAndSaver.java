package com.github.franckteddev.search.fetchandsave;

import com.github.franckteddev.search.db.CanonicalIngredientRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import static com.github.franckteddev.search.utility.Normalizer.normalize;

public class CanonicalIngredientFetcherAndSaver extends FetcherAndSaver{

    public CanonicalIngredientFetcherAndSaver(Connection connection) {
        super(connection);
    }

    @Override
    public void execute() throws IOException, SQLException {
        CanonicalIngredientRepository repository = new CanonicalIngredientRepository(super.connection);
        try(InputStream is = CanonicalIngredientFetcherAndSaver.class.getResourceAsStream("/ingredients_canonical.txt")){
            if(is == null){
                throw new IOException("Unable to find ingredients_canonical.txt on the classpath");
            }
            try(BufferedReader br = new BufferedReader(new InputStreamReader(is))){
                String line;
                while((line = br.readLine()) != null){
                    if(line.isBlank()){
                        continue;
                    }
                    String[] parts = line.split(":");
                    String canonicalIngredient = normalize(parts[0]);
                    repository.save(canonicalIngredient);
                }
            }
        }
    }
}
