package com.depichan.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.depichan.db.DBConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private Label labelSalesToday;
    @FXML private Label labelSalesTodayTrx;
    @FXML private Label labelLowStock;
    @FXML private Label labelInventoryValue;
    @FXML private Label labelTotalItems;
    @FXML private Label labelSalesWeek;
    @FXML private Label labelSalesWeekTrx;
    @FXML private Hyperlink linkLowStockDetail;
    
    @FXML private BarChart<String, Number> salesTrendChart;
    @FXML private CategoryAxis salesTrendX;
    @FXML private NumberAxis salesTrendY;
    
    @FXML private TableView<TopProduct> topProductsTable;
    @FXML private TableColumn<TopProduct, String> colProductName;
    @FXML private TableColumn<TopProduct, Integer> colProductQty;
    @FXML private TableColumn<TopProduct, Double> colProductTotal;
    
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
    
    @FXML
    public void initialize() {
        // Set up table columns
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProductQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colProductTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Make table responsive with dynamic column sizing
        topProductsTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue() - 20; // Account for padding and scrollbar
            colProductName.setPrefWidth(tableWidth * 0.5); // 50% of table width
            colProductQty.setPrefWidth(tableWidth * 0.25);   // 25% of table width
            colProductTotal.setPrefWidth(tableWidth * 0.25); // 25% of table width
        });
        
        // Format the currency column
        colProductTotal.setCellFactory(column -> new TableCell<TopProduct, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(total));
                }
            }
        });
        
        // Format quantity column for better alignment
        colProductQty.setCellFactory(column -> new TableCell<TopProduct, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(quantity));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        // Format product name column
        colProductName.setCellFactory(column -> new TableCell<TopProduct, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                } else {
                    setText(name);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 8 0 8;");
                }
            }
        });
        
        // Load data
        refreshDashboard();
    }
    
    @FXML
    public void refreshDashboard() {
        loadSalesMetrics();
        loadInventoryMetrics();
        loadSalesTrendChart();
        loadTopProducts();
    }
    
    private void loadSalesMetrics() {
        try (Connection conn = DBConnection.getConnection()) {
            // Today's sales
            String todayQuery = "SELECT COUNT(*) as count, SUM(jumlah * harga_satuan) as total " +
                               "FROM penjualan WHERE DATE(tanggal) = CURDATE()";
            
            PreparedStatement todayStmt = conn.prepareStatement(todayQuery);
            ResultSet todayRs = todayStmt.executeQuery();
            
            if (todayRs.next()) {
                double total = todayRs.getDouble("total");
                int count = todayRs.getInt("count");
                
                labelSalesToday.setText(currencyFormat.format(total));
                labelSalesTodayTrx.setText(count + " transaksi");
            }
            
            // This week's sales
            String weekQuery = "SELECT COUNT(*) as count, SUM(jumlah * harga_satuan) as total " +
                              "FROM penjualan WHERE tanggal >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            
            PreparedStatement weekStmt = conn.prepareStatement(weekQuery);
            ResultSet weekRs = weekStmt.executeQuery();
            
            if (weekRs.next()) {
                double total = weekRs.getDouble("total");
                int count = weekRs.getInt("count");
                
                labelSalesWeek.setText(currencyFormat.format(total));
                labelSalesWeekTrx.setText(count + " transaksi");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load sales metrics", e);
            showAlert("Gagal memuat data penjualan: " + e.getMessage());
        }
    }
    
    private void loadInventoryMetrics() {
        try (Connection conn = DBConnection.getConnection()) {
            // Total inventory value
            String valueQuery = "SELECT COUNT(*) as count, SUM(stok * harga) as total " +
                               "FROM barang";
            
            PreparedStatement valueStmt = conn.prepareStatement(valueQuery);
            ResultSet valueRs = valueStmt.executeQuery();
            
            if (valueRs.next()) {
                double total = valueRs.getDouble("total");
                int count = valueRs.getInt("count");
                
                labelInventoryValue.setText(currencyFormat.format(total));
                labelTotalItems.setText(count + " barang");
            }
            
            // Low stock items (less than 10 units)
            String lowStockQuery = "SELECT COUNT(*) as count FROM barang WHERE stok < 10";
            
            PreparedStatement lowStockStmt = conn.prepareStatement(lowStockQuery);
            ResultSet lowStockRs = lowStockStmt.executeQuery();
            
            if (lowStockRs.next()) {
                int count = lowStockRs.getInt("count");
                labelLowStock.setText(count + " barang");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load inventory metrics", e);
            showAlert("Gagal memuat data inventaris: " + e.getMessage());
        }
    }
    
    private void loadSalesTrendChart() {
        try (Connection conn = DBConnection.getConnection()) {
            // Clear previous data
            salesTrendChart.getData().clear();
            
            // Get sales for the last 7 days
            String query = "SELECT DATE(tanggal) as date, SUM(jumlah * harga_satuan) as total " +
                          "FROM penjualan " +
                          "WHERE tanggal >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                          "GROUP BY DATE(tanggal) " +
                          "ORDER BY DATE(tanggal)";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            // Create chart series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Penjualan");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                double total = rs.getDouble("total");
                series.getData().add(new XYChart.Data<>(date.format(formatter), total));
            }
            
            // If we have less than 7 days of data, add empty days
            if (series.getData().size() < 7) {
                LocalDate today = LocalDate.now();
                
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    String dateStr = date.format(formatter);
                    
                    // Check if this date is already in the series
                    boolean exists = false;
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        if (data.getXValue().equals(dateStr)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    // Add with zero value if it doesn't exist
                    if (!exists) {
                        series.getData().add(new XYChart.Data<>(dateStr, 0));
                    }
                }
                
                // Sort the series by date
                series.getData().sort((a, b) -> {
                    try {
                        // Add current year to make valid LocalDate objects for comparison
                        int currentYear = LocalDate.now().getYear();
                        
                        // Parse dates with the year added
                        LocalDate dateA = LocalDate.parse(
                            a.getXValue() + "/" + currentYear, 
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        );
                        
                        LocalDate dateB = LocalDate.parse(
                            b.getXValue() + "/" + currentYear, 
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        );
                        
                        return dateA.compareTo(dateB);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error parsing date for chart sorting", e);
                        return 0;
                    }
                });
            }
            
            salesTrendChart.getData().add(series);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load sales trend data", e);
            showAlert("Gagal memuat data tren penjualan: " + e.getMessage());
        }
    }
    
    private void loadTopProducts() {
        try (Connection conn = DBConnection.getConnection()) {
            // Get top selling products for current month
            String query = "SELECT b.nama_barang, SUM(p.jumlah) as total_qty, " +
                          "SUM(p.jumlah * p.harga_satuan) as total_sales " +
                          "FROM penjualan p " +
                          "JOIN barang b ON p.id_barang = b.id_barang " +
                          "WHERE MONTH(p.tanggal) = MONTH(CURRENT_DATE()) " +
                          "AND YEAR(p.tanggal) = YEAR(CURRENT_DATE()) " +
                          "GROUP BY b.id_barang " +
                          "ORDER BY total_sales DESC " +
                          "LIMIT 10";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<TopProduct> data = FXCollections.observableArrayList();
            
            while (rs.next()) {
                data.add(new TopProduct(
                    rs.getString("nama_barang"),
                    rs.getInt("total_qty"),
                    rs.getDouble("total_sales")
                ));
            }
            
            topProductsTable.setItems(data);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load top products data", e);
            showAlert("Gagal memuat data produk terlaris: " + e.getMessage());
        }
    }
    
    @FXML
    public void showLowStockItems() {
        try {
            // Load low stock items
            ObservableList<LowStockItem> items = getLowStockItems();
            
            if (items.isEmpty()) {
                showAlert("Tidak ada barang dengan stok menipis (< 10)");
                return;
            }
            
            // Create a responsive table view
            TableView<LowStockItem> tableView = new TableView<>();
            tableView.setItems(items);
            
            TableColumn<LowStockItem, Integer> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colId.setStyle("-fx-alignment: CENTER;");
            
            TableColumn<LowStockItem, String> colName = new TableColumn<>("Nama Barang");
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colName.setStyle("-fx-alignment: CENTER-LEFT;");
            
            TableColumn<LowStockItem, String> colCategory = new TableColumn<>("Kategori");
            colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
            colCategory.setStyle("-fx-alignment: CENTER;");
            
            TableColumn<LowStockItem, Integer> colStock = new TableColumn<>("Stok");
            colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
            colStock.setStyle("-fx-alignment: CENTER;");
            
            // Add columns individually to avoid warning
            tableView.getColumns().add(colId);
            tableView.getColumns().add(colName);
            tableView.getColumns().add(colCategory);
            tableView.getColumns().add(colStock);
            
            // Make the table responsive
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            
            // Dynamic column sizing
            tableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                double tableWidth = newWidth.doubleValue() - 20;
                colId.setPrefWidth(tableWidth * 0.1);        // 10%
                colName.setPrefWidth(tableWidth * 0.5);      // 50%
                colCategory.setPrefWidth(tableWidth * 0.25); // 25%
                colStock.setPrefWidth(tableWidth * 0.15);    // 15%
            });
            
            // Create responsive dialog
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Daftar Barang Stok Menipis");
            dialog.setMinWidth(600);
            dialog.setMinHeight(400);
            
            VBox vbox = new VBox(15);
            vbox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20;");
            
            // Header
            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label headerIcon = new Label("⚠️");
            headerIcon.setStyle("-fx-font-size: 24px;");
            Label headerText = new Label("Daftar barang dengan stok kurang dari 10:");
            headerText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
            header.getChildren().addAll(headerIcon, headerText);
            
            // Close button
            Button closeButton = new Button("Tutup");
            closeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f44336, #d32f2f); " +
                               "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                               "-fx-background-radius: 20; -fx-border-radius: 20; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
            closeButton.setPrefHeight(35);
            closeButton.setPrefWidth(100);
            closeButton.setOnAction(event -> dialog.close());
            
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
            buttonBox.getChildren().add(closeButton);
            
            vbox.getChildren().addAll(header, tableView, buttonBox);
            VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);
            
            Scene scene = new Scene(vbox, 700, 500);
            dialog.setScene(scene);
            
            // Make dialog resizable and center it
            dialog.setResizable(true);
            dialog.centerOnScreen();
            dialog.show();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load low stock items", e);
            showAlert("Gagal memuat data barang stok menipis: " + e.getMessage());
        }
    }
    
    private ObservableList<LowStockItem> getLowStockItems() throws SQLException {
        ObservableList<LowStockItem> items = FXCollections.observableArrayList();
        
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT id_barang, nama_barang, kategori, stok " +
                          "FROM barang WHERE stok < 10 ORDER BY stok ASC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                items.add(new LowStockItem(
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    rs.getInt("stok")
                ));
            }
        }
        
        return items;
    }
    
    @FXML
    public void goToProductManagement() {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("/view/barang-view.fxml"));
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) labelSalesToday.getScene().getWindow();
            stage.setTitle("Data Barang DepiStore");
            stage.setScene(scene);
            
            // Set ukuran window yang konsisten dan tidak mepet
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open product management page", e);
            showAlert("Gagal membuka halaman kelola barang: " + e.getMessage());
        }
    }
    
    // Menghapus fungsi generateReport() karena sudah ada di BarangController
    
    private void showAlert(String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }
    
    // Model classes for the dashboard
    public static class TopProduct {
        private final String name;
        private final int quantity;
        private final double total;
        
        public TopProduct(String name, int quantity, double total) {
            this.name = name;
            this.quantity = quantity;
            this.total = total;
        }
        
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return total; }
    }
    
    public static class LowStockItem {
        private final int id;
        private final String name;
        private final String category;
        private final int stock;
        
        public LowStockItem(int id, String name, String category, int stock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.stock = stock;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getStock() { return stock; }
    }
}
