package com.github.franckteddev.search.fetchandsave;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class FetcherAndSaver {
    protected final Connection connection;

    public FetcherAndSaver(Connection connection) {
        this.connection = connection;
    }

    public abstract void execute() throws IOException, SQLException, InterruptedException;
}
