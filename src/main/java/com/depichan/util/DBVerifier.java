package com.depichan.util;

import com.depichan.db.DBConnection;

/**
 * A simple utility to verify the database connection and table structure.
 * This is a standalone program you can run to test things before running the full application.
 */
public class DBVerifier {
    public static void main(String[] args) {
        System.out.println("\n=== DATABASE VERIFIER ===");
        
        // Test connection
        System.out.println("\nTesting database connection:");
        boolean isConnected = DBConnection.testConnection();
        System.out.println("Connection status: " + (isConnected ? "SUCCESS" : "FAILED"));
        
        if (isConnected) {
            // Check tables
            System.out.println("\nChecking table structures:");
            
            // Check penjualan table
            DBTester.checkPenjualanTable();
            
            // Check sample data
            System.out.println("\nChecking sample data:");
            DBTester.checkPenjualanData();
            
            // Test the query for today
            System.out.println("\nTesting sales query for today:");
            java.time.LocalDate today = java.time.LocalDate.now();
            DBTester.testSalesQuery(today, today);
        }
        
        System.out.println("\n=== VERIFICATION COMPLETE ===");
    }
}
