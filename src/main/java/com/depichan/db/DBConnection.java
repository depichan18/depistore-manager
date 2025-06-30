// File: src/main/java/com/depichan/db/DBConnection.java
package com.depichan.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/tokodepi";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // kosong jika default XAMPP

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Koneksi ke database gagal");
        }
    }
}