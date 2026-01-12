package com.liga.repository.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnection {
    private static PostgresConnection instance;
    private Connection connection;

    // Config defaults (matching import_json_to_postgres.py)
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DB = "la_liga";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASS = "admin";

    private PostgresConnection() {
        try {
            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : DEFAULT_HOST;
            String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : DEFAULT_PORT;
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : DEFAULT_DB;
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER;
            String pass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : DEFAULT_PASS;

            String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
            
            // Load driver explictiy
            Class.forName("org.postgresql.Driver");
            
            this.connection = DriverManager.getConnection(url, user, pass);
            System.out.println("✔ Conexión a PostgreSQL establecida: " + url);

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("✘ Error conectando a PostgreSQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized PostgresConnection getInstance() {
        if (instance == null) {
            instance = new PostgresConnection();
        }
        try {
             if (instance.connection == null || instance.connection.isClosed()) {
                 instance = new PostgresConnection();
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
