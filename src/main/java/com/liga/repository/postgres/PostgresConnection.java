package com.liga.repository.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/la_liga";
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";

    private PostgresConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
