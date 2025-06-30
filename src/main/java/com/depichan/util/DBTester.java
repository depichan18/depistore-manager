package com.depichan.util;

import com.depichan.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class untuk testing koneksi database dan query.
 * Digunakan untuk debugging masalah laporan penjualan.
 */
public class DBTester {
    
    /**
     * Test query untuk mendapatkan data penjualan.
     */
    public static void testSalesQuery(LocalDate startDate, LocalDate endDate) {
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        System.out.println("Test query penjualan dari " + startDateStr + " sampai " + endDateStr);
        
        String query = "SELECT p.id, b.nama_barang, b.kategori, p.jumlah, p.harga_satuan, " +
                    "(p.jumlah * p.harga_satuan) as total, p.tanggal " +
                    "FROM penjualan p " +
                    "JOIN barang b ON p.id_barang = b.id_barang " +
                    "WHERE DATE(p.tanggal) BETWEEN ? AND ? " +
                    "ORDER BY p.tanggal";
        
        System.out.println("Query: " + query);
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("ERROR: Koneksi database null");
                return;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, startDateStr);
                stmt.setString(2, endDateStr);
                
                System.out.println("Executing query with parameters: " + startDateStr + ", " + endDateStr);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("Record #" + count + ": " +
                            "ID=" + rs.getInt("id") + 
                            ", Barang=" + rs.getString("nama_barang") +
                            ", Jumlah=" + rs.getInt("jumlah") +
                            ", Total=" + rs.getDouble("total") +
                            ", Tanggal=" + rs.getTimestamp("tanggal"));
                    }
                    
                    if (count == 0) {
                        System.out.println("Tidak ada data penjualan untuk periode yang dipilih");
                    } else {
                        System.out.println("Total " + count + " record ditemukan");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("ERROR SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test untuk memeriksa struktur tabel penjualan.
     */
    public static void checkPenjualanTable() {
        System.out.println("Memeriksa struktur tabel penjualan...");
        
        String query = "DESCRIBE penjualan";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("Struktur tabel penjualan:");
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " - " + 
                                   rs.getString("Type") + " - " +
                                   (rs.getString("Null").equals("YES") ? "NULL" : "NOT NULL") + " - " +
                                   (rs.getString("Key").equals("PRI") ? "PRIMARY KEY" : rs.getString("Key")));
            }
            
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test data sample pada tabel penjualan.
     */
    public static void checkPenjualanData() {
        System.out.println("Memeriksa data penjualan...");
        
        String query = "SELECT COUNT(*) as count FROM penjualan";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("Total data penjualan: " + count);
                
                if (count > 0) {
                    // Tampilkan beberapa sample data
                    String sampleQuery = "SELECT * FROM penjualan LIMIT 5";
                    try (PreparedStatement sampleStmt = conn.prepareStatement(sampleQuery);
                         ResultSet sampleRs = sampleStmt.executeQuery()) {
                        
                        System.out.println("Sample data penjualan:");
                        while (sampleRs.next()) {
                            System.out.println(
                                "ID=" + sampleRs.getInt("id") + 
                                ", ID Barang=" + sampleRs.getInt("id_barang") +
                                ", Jumlah=" + sampleRs.getInt("jumlah") +
                                ", Harga=" + sampleRs.getDouble("harga_satuan") +
                                ", Tanggal=" + sampleRs.getTimestamp("tanggal")
                            );
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
