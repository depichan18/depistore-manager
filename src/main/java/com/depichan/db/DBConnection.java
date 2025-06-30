// File: src/main/java/com/depichan/db/DBConnection.java
package com.depichan.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    
    private static final String URL = "jdbc:mysql://localhost:3306/tokodepi";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // kosong jika default XAMPP

    static {
        try {
            // Register driver saat class ini dimuat
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            System.err.println("Error: MySQL JDBC Driver tidak ditemukan");
        }
    }

    public static Connection getConnection() {
        try {
            // Buat koneksi
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            if (conn != null) {
                LOGGER.info("Database connection established successfully");
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database", e);
            System.err.println("Error koneksi DB: " + e.getMessage());
            return null; // Return null untuk ditangani oleh pemanggil
        }
    }
    
    // Test koneksi database - gunakan untuk debugging
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            System.err.println("Test koneksi gagal: " + e.getMessage());
            return false;
        }
    }
}