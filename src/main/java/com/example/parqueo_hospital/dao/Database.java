package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.ParkingException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/parqueo_hospital";

    private Database() { }

    public static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("PARKING_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("PARKING_DB_USER", "postgres");
        String password = System.getenv().getOrDefault("PARKING_DB_PASSWORD", "postgres");
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection(url, user, password);
    }

    public static void verifyConnection() {
        try (Connection ignored = connect()) {
            // Opening the connection verifies the configured JDBC credentials.
        } catch (SQLException e) {
            throw new ParkingException("No se pudo conectar con PostgreSQL. Revise las variables PARKING_DB_URL, PARKING_DB_USER y PARKING_DB_PASSWORD.", e);
        }
    }
}
