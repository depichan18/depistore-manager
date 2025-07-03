package com.depichan.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.depichan.db.DBConnection;
import com.depichan.model.Barang;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class BarangController {

    private static final Logger LOGGER = Logger.getLogger(BarangController.class.getName());

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbSortBy;
    @FXML private ComboBox<String> cbSortOrder;
    @FXML private TableView<Barang> tableBarang;
    @FXML private TableColumn<Barang, Integer> colId;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, String> colKategori;
    @FXML private TableColumn<Barang, Integer> colHarga;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, String> colTanggal;

    private ObservableList<Barang> dataBarang;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        // Make table responsive with dynamic column sizing
        tableBarang.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue() - 20; // Account for padding and scrollbar
            
            // Distribute column widths proportionally
            colId.setPrefWidth(tableWidth * 0.08);        // 8% - ID column
            colNama.setPrefWidth(tableWidth * 0.35);      // 35% - Nama Barang
            colKategori.setPrefWidth(tableWidth * 0.18);  // 18% - Kategori
            colHarga.setPrefWidth(tableWidth * 0.15);     // 15% - Harga
            colStok.setPrefWidth(tableWidth * 0.09);      // 9% - Stok
            colTanggal.setPrefWidth(tableWidth * 0.15);   // 15% - Tanggal
        });

        cbSortBy.setItems(FXCollections.observableArrayList("Nama", "Harga", "Stok"));
        cbSortOrder.setItems(FXCollections.observableArrayList("ASC", "DESC"));

        cbSortBy.setValue("Nama");
        cbSortOrder.setValue("ASC");

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData());
        cbSortBy.setOnAction(e -> loadData());
        cbSortOrder.setOnAction(e -> loadData());

        // Setup context menu untuk tabel
        setupTableContextMenu();

        // Setup zebra striping
        tableBarang.setRowFactory(createTableRowFactory());
        
        // Apply zebra striping style to table
        tableBarang.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-selection-bar: #bbdefb; " +                   // biru muda sedikit lebih gelap saat dipilih
            "-fx-selection-bar-non-focused: #e3f2fd;"          // biru muda biasa saat tidak fokus
        );
        
        // Style header columns dengan warna biru gelap
        colId.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
        colNama.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
        colKategori.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
        colHarga.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
        colStok.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
        colTanggal.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
        
        // Setup cell factories untuk mencegah konflik dengan zebra striping
        setupCellFactories();

        loadData();
    }

    public void loadData() {
        dataBarang = FXCollections.observableArrayList();

        String keyword = tfSearch.getText();
        String sortBy = cbSortBy.getValue().toLowerCase();
        String sortOrder = cbSortOrder.getValue();

        if (sortBy.equals("nama")) sortBy = "nama_barang";
        if (sortBy.equals("harga")) sortBy = "harga";
        if (sortBy.equals("stok")) sortBy = "stok";

        String query = "SELECT * FROM barang WHERE nama_barang LIKE ? ORDER BY " + sortBy + " " + sortOrder;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataBarang.add(new Barang(
                        rs.getInt("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("kategori"),
                        rs.getInt("harga"),
                        rs.getInt("stok"),
                        rs.getDate("tanggal_masuk").toString()
                ));
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading data", e);
        }

        tableBarang.setItems(dataBarang);
    }

    @FXML
    public void onRefresh() {
        tfSearch.clear();
        cbSortBy.setValue("Nama");
        cbSortOrder.setValue("ASC");
        loadData();
    }

    @FXML
    public void onJualBarang() {
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih barang terlebih dahulu.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Jual Barang");
        dialog.setHeaderText("Masukkan jumlah yang ingin dijual:");
        dialog.setContentText("Jumlah:");

        dialog.showAndWait().ifPresent(jumlahStr -> {
            try {
                int jumlah = Integer.parseInt(jumlahStr);
                if (jumlah <= 0) {
                    showAlert("Jumlah harus lebih dari 0.");
                    return;
                }
                if (jumlah > selected.getStok()) {
                    showAlert("Stok tidak mencukupi.");
                    return;
                }

                Connection conn = DBConnection.getConnection();

                // Update stok
                String updateStok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";
                PreparedStatement ps1 = conn.prepareStatement(updateStok);
                ps1.setInt(1, jumlah);
                ps1.setInt(2, selected.getId());
                ps1.executeUpdate();

                // Catat transaksi
                String insertPenjualan = "INSERT INTO penjualan (id_barang, jumlah, harga_satuan, tanggal) VALUES (?, ?, ?, NOW())";
                PreparedStatement ps2 = conn.prepareStatement(insertPenjualan);
                ps2.setInt(1, selected.getId());
                ps2.setInt(2, jumlah);
                ps2.setDouble(3, selected.getHarga());
                ps2.executeUpdate();

                showAlert("Penjualan berhasil disimpan.");
                loadData();

                // Tampilkan Nota
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Nota.fxml"));
                AnchorPane notaRoot = loader.load();
                NotaController notaController = loader.getController();

                Stage stage = new Stage();
                stage.setTitle("Nota Penjualan");
                stage.setScene(new Scene(notaRoot));
                notaController.setStage(stage);

                double total = jumlah * selected.getHarga();
                String waktu = java.time.LocalDateTime.now().toString();

                notaController.setNotaData(selected.getNama(), jumlah, selected.getHarga(), total, waktu);
                stage.show();
                
                // Simpan nota ke direktori downloads dengan format yang sesuai
                notaController.saveNotaAsPDFToDefault();

            } catch (NumberFormatException e) {
                showAlert("Masukkan angka yang valid.");
            } catch (SQLException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error during sale transaction", e);
                showAlert("Gagal menyimpan penjualan atau menampilkan nota.");
            }
        });
    }

    @FXML
    public void onTambahStok() {
        // Membuat dialog custom untuk input banyak barang
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Tambah Stok Barang");
        dialog.setHeaderText("Masukkan data dalam format: ID,Jumlah (satu baris per barang)");

        // Set up the button types
        ButtonType okButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create the text area for input
        TextArea textArea = new TextArea();
        textArea.setPromptText("Contoh:\n1,50\n2,30\n3,100");
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(30);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(
            new Label("Format: ID_Barang,Jumlah_Stok"), 
            new Label("Contoh: 1,50 (untuk menambah 50 stok pada barang ID 1)"),
            textArea
        );
        vbox.setSpacing(5);

        dialog.getDialogPane().setContent(vbox);

        // Convert the result when the button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return textArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(input -> {
            if (input.trim().isEmpty()) {
                showAlert("Input tidak boleh kosong.");
                return;
            }

            String[] lines = input.trim().split("\n");
            int berhasil = 0;
            int gagal = 0;
            StringBuilder pesanError = new StringBuilder();

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false); // Start transaction

                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(",");
                    if (parts.length != 2) {
                        gagal++;
                        pesanError.append("Format salah: ").append(line).append("\n");
                        continue;
                    }

                    try {
                        int idBarang = Integer.parseInt(parts[0].trim());
                        int jumlahStok = Integer.parseInt(parts[1].trim());

                        if (jumlahStok <= 0) {
                            gagal++;
                            pesanError.append("Jumlah stok harus > 0 untuk ID: ").append(idBarang).append("\n");
                            continue;
                        }

                        // Cek apakah barang ada
                        String checkQuery = "SELECT nama_barang FROM barang WHERE id_barang = ?";
                        PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                        checkPs.setInt(1, idBarang);
                        ResultSet rs = checkPs.executeQuery();

                        if (!rs.next()) {
                            gagal++;
                            pesanError.append("Barang dengan ID ").append(idBarang).append(" tidak ditemukan\n");
                            continue;
                        }

                        // Update stok
                        String updateQuery = "UPDATE barang SET stok = stok + ? WHERE id_barang = ?";
                        PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                        updatePs.setInt(1, jumlahStok);
                        updatePs.setInt(2, idBarang);

                        int rowsAffected = updatePs.executeUpdate();
                        if (rowsAffected > 0) {
                            berhasil++;
                        } else {
                            gagal++;
                            pesanError.append("Gagal update stok untuk ID: ").append(idBarang).append("\n");
                        }

                    } catch (NumberFormatException e) {
                        gagal++;
                        pesanError.append("Format angka salah: ").append(line).append("\n");
                    }
                }

                conn.commit(); // Commit transaction
                conn.setAutoCommit(true);

                String hasil = """
                               Hasil update stok:
                               Berhasil: %d barang
                               Gagal: %d barang
                               """.formatted(berhasil, gagal);

                if (pesanError.length() > 0) {
                    hasil += "\n\nDetail Error:\n" + pesanError;
                }

                showAlert(hasil);
                loadData(); // Refresh tabel

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error updating stock", e);
                showAlert("Gagal melakukan update stok: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onTambahBarang() {
        // Membuat dialog custom untuk input barang baru
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Tambah Barang Baru");
        dialog.setHeaderText("Masukkan informasi barang baru");

        // Set up the button types
        ButtonType okButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create form fields
        TextField tfNama = new TextField();
        tfNama.setPromptText("Nama barang");
        
        ComboBox<String> cbKategori = new ComboBox<>();
        cbKategori.setEditable(true);
        cbKategori.setPromptText("Pilih kategori yang ada atau ketik kategori baru");
        
        TextField tfHarga = new TextField();
        tfHarga.setPromptText("Harga");
        
        TextField tfStok = new TextField();
        tfStok.setPromptText("Stok awal");

        // Load existing categories
        loadKategoriToComboBox(cbKategori);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(
            new Label("Nama Barang:"), tfNama,
            new Label("Kategori:"), cbKategori,
            new Label("Harga:"), tfHarga,
            new Label("Stok Awal:"), tfStok
        );
        vbox.setSpacing(10);

        dialog.getDialogPane().setContent(vbox);

        // Convert the result when the button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String nama = tfNama.getText().trim();
                String kategori = cbKategori.getValue();
                String harga = tfHarga.getText().trim();
                String stok = tfStok.getText().trim();
                
                return nama + "," + kategori + "," + harga + "," + stok;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length != 4) {
                showAlert("Data tidak lengkap.");
                return;
            }

            String nama = parts[0].trim();
            String kategori = parts[1].trim();
            String hargaStr = parts[2].trim();
            String stokStr = parts[3].trim();

            // Validasi input
            if (nama.isEmpty() || kategori.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                showAlert("Semua field harus diisi.");
                return;
            }

            try {
                int harga = Integer.parseInt(hargaStr);
                int stok = Integer.parseInt(stokStr);

                if (harga <= 0 || stok < 0) {
                    showAlert("Harga harus lebih dari 0 dan stok tidak boleh negatif.");
                    return;
                }

                // Simpan ke database
                try (Connection conn = DBConnection.getConnection()) {
                    // Cek apakah nama barang sudah ada
                    String checkQuery = "SELECT COUNT(*) FROM barang WHERE nama_barang = ?";
                    PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                    checkPs.setString(1, nama);
                    ResultSet rs = checkPs.executeQuery();
                    
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert("Barang dengan nama '" + nama + "' sudah ada.");
                        return;
                    }

                    // Insert barang baru
                    String insertQuery = "INSERT INTO barang (nama_barang, kategori, harga, stok, tanggal_masuk) VALUES (?, ?, ?, ?, NOW())";
                    PreparedStatement insertPs = conn.prepareStatement(insertQuery);
                    insertPs.setString(1, nama);
                    insertPs.setString(2, kategori);
                    insertPs.setInt(3, harga);
                    insertPs.setInt(4, stok);

                    int rowsAffected = insertPs.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("Barang '" + nama + "' berhasil ditambahkan.");
                        loadData(); // Refresh tabel
                    } else {
                        showAlert("Gagal menambah barang.");
                    }

                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error saving new product", e);
                    showAlert("Gagal menyimpan barang: " + e.getMessage());
                }

            } catch (NumberFormatException e) {
                showAlert("Harga dan stok harus berupa angka yang valid.");
            }
        });
    }

    private void loadKategoriToComboBox(ComboBox<String> comboBox) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT DISTINCT kategori FROM barang ORDER BY kategori";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            ObservableList<String> kategoriList = FXCollections.observableArrayList();
            while (rs.next()) {
                String kategori = rs.getString("kategori");
                if (kategori != null && !kategori.trim().isEmpty()) {
                    kategoriList.add(kategori);
                }
            }
            comboBox.setItems(kategoriList);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading categories", e);
            showAlert("Gagal memuat kategori: " + e.getMessage());
        }
    }

    private void showAlert(String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    private void setupTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit Barang");
        editItem.setOnAction(e -> editBarang());

        MenuItem tambahStokItem = new MenuItem("Tambah Stok");
        tambahStokItem.setOnAction(e -> tambahStokManual());

        MenuItem hapusItem = new MenuItem("Hapus Barang");
        hapusItem.setOnAction(e -> hapusBarang());

        contextMenu.getItems().addAll(editItem, tambahStokItem, hapusItem);
        tableBarang.setContextMenu(contextMenu);
    }

    private void editBarang() {
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih barang terlebih dahulu.");
            return;
        }

        // Membuat dialog custom untuk edit barang
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Barang");
        dialog.setHeaderText("Edit informasi barang: " + selected.getNama());

        // Set up the button types
        ButtonType okButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create form fields with current values
        TextField tfNama = new TextField(selected.getNama());
        tfNama.setPromptText("Nama barang");
        
        ComboBox<String> cbKategori = new ComboBox<>();
        cbKategori.setEditable(true);
        cbKategori.setValue(selected.getKategori());
        loadKategoriToComboBox(cbKategori);
        
        TextField tfHarga = new TextField(String.valueOf(selected.getHarga()));
        tfHarga.setPromptText("Harga");
        
        TextField tfStok = new TextField(String.valueOf(selected.getStok()));
        tfStok.setPromptText("Stok");

        VBox vbox = new VBox();
        vbox.getChildren().addAll(
            new Label("Nama Barang:"), tfNama,
            new Label("Kategori:"), cbKategori,
            new Label("Harga:"), tfHarga,
            new Label("Stok:"), tfStok
        );
        vbox.setSpacing(10);

        dialog.getDialogPane().setContent(vbox);

        // Convert the result when the button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String nama = tfNama.getText().trim();
                String kategori = cbKategori.getValue();
                String harga = tfHarga.getText().trim();
                String stok = tfStok.getText().trim();
                
                return nama + "," + kategori + "," + harga + "," + stok;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length != 4) {
                showAlert("Data tidak lengkap.");
                return;
            }

            String nama = parts[0].trim();
            String kategori = parts[1].trim();
            String hargaStr = parts[2].trim();
            String stokStr = parts[3].trim();

            // Validasi input
            if (nama.isEmpty() || kategori.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                showAlert("Semua field harus diisi.");
                return;
            }

            try {
                int harga = Integer.parseInt(hargaStr);
                int stok = Integer.parseInt(stokStr);

                if (harga <= 0 || stok < 0) {
                    showAlert("Harga harus lebih dari 0 dan stok tidak boleh negatif.");
                    return;
                }

                // Update ke database
                try (Connection conn = DBConnection.getConnection()) {
                    // Cek apakah nama barang sudah ada (kecuali barang yang sedang diedit)
                    if (!nama.equals(selected.getNama())) {
                        String checkQuery = "SELECT COUNT(*) FROM barang WHERE nama_barang = ? AND id_barang != ?";
                        PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                        checkPs.setString(1, nama);
                        checkPs.setInt(2, selected.getId());
                        ResultSet rs = checkPs.executeQuery();
                        
                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Barang dengan nama '" + nama + "' sudah ada.");
                            return;
                        }
                    }

                    // Update barang
                    String updateQuery = "UPDATE barang SET nama_barang = ?, kategori = ?, harga = ?, stok = ? WHERE id_barang = ?";
                    PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                    updatePs.setString(1, nama);
                    updatePs.setString(2, kategori);
                    updatePs.setInt(3, harga);
                    updatePs.setInt(4, stok);
                    updatePs.setInt(5, selected.getId());

                    int rowsAffected = updatePs.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("Barang '" + nama + "' berhasil diupdate.");
                        loadData(); // Refresh tabel
                    } else {
                        showAlert("Gagal mengupdate barang.");
                    }

                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error updating product", e);
                    showAlert("Gagal mengupdate barang: " + e.getMessage());
                }

            } catch (NumberFormatException e) {
                showAlert("Harga dan stok harus berupa angka yang valid.");
            }
        });
    }

    private void tambahStokManual() {
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih barang terlebih dahulu.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tambah Stok");
        dialog.setHeaderText("Menambah stok untuk: " + selected.getNama());
        dialog.setContentText("Jumlah stok yang ditambahkan:");

        dialog.showAndWait().ifPresent(jumlahStr -> {
            try {
                int jumlah = Integer.parseInt(jumlahStr);
                if (jumlah <= 0) {
                    showAlert("Jumlah harus lebih dari 0.");
                    return;
                }

                try (Connection conn = DBConnection.getConnection()) {
                    String updateStok = "UPDATE barang SET stok = stok + ? WHERE id_barang = ?";
                    PreparedStatement ps = conn.prepareStatement(updateStok);
                    ps.setInt(1, jumlah);
                    ps.setInt(2, selected.getId());
                    
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert("Stok berhasil ditambahkan. Stok baru: " + (selected.getStok() + jumlah));
                        loadData(); // Refresh tabel
                    } else {
                        showAlert("Gagal menambah stok.");
                    }

                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error adding stock", e);
                    showAlert("Gagal menambah stok: " + e.getMessage());
                }

            } catch (NumberFormatException e) {
                showAlert("Masukkan angka yang valid.");
            }
        });
    }

    private void hapusBarang() {
        Barang selected = tableBarang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih barang terlebih dahulu.");
            return;
        }

        // Konfirmasi pertama
        Alert firstConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        firstConfirm.setTitle("Konfirmasi Hapus - Tahap 1");
        firstConfirm.setHeaderText("Hapus barang '" + selected.getNama() + "'?");
        firstConfirm.setContentText("Anda akan menghapus barang ini beserta riwayat penjualannya. Lanjutkan?");

        firstConfirm.showAndWait().ifPresent(response1 -> {
            if (response1 == ButtonType.OK) {
                // Konfirmasi kedua
                Alert secondConfirm = new Alert(Alert.AlertType.WARNING);
                secondConfirm.setTitle("Konfirmasi Hapus - Tahap 2");
                secondConfirm.setHeaderText("PERINGATAN!");
                secondConfirm.setContentText("Anda yakin ingin menghapus barang '" + selected.getNama() + "'?\n\n" +
                                           "Data yang akan dihapus:\n" +
                                           "- Barang: " + selected.getNama() + "\n" +
                                           "- Kategori: " + selected.getKategori() + "\n" +
                                           "- Harga: Rp " + selected.getHarga() + "\n" +
                                           "- Stok: " + selected.getStok() + "\n\n" +
                                           "Aksi ini TIDAK DAPAT DIBATALKAN!");

                secondConfirm.showAndWait().ifPresent(response2 -> {
                    if (response2 == ButtonType.OK) {
                        try (Connection conn = DBConnection.getConnection()) {
                            conn.setAutoCommit(false); // Start transaction

                            // Hapus riwayat penjualan terlebih dahulu
                            String deletePenjualan = "DELETE FROM penjualan WHERE id_barang = ?";
                            PreparedStatement ps1 = conn.prepareStatement(deletePenjualan);
                            ps1.setInt(1, selected.getId());
                            int penjualanDeleted = ps1.executeUpdate();

                            // Hapus barang
                            String deleteBarang = "DELETE FROM barang WHERE id_barang = ?";
                            PreparedStatement ps2 = conn.prepareStatement(deleteBarang);
                            ps2.setInt(1, selected.getId());
                            int barangDeleted = ps2.executeUpdate();

                            if (barangDeleted > 0) {
                                conn.commit(); // Commit transaction
                                showAlert("Barang '" + selected.getNama() + "' berhasil dihapus.\n" +
                                         "Riwayat penjualan yang dihapus: " + penjualanDeleted + " record.");
                                loadData(); // Refresh tabel
                            } else {
                                conn.rollback(); // Rollback if failed
                                showAlert("Gagal menghapus barang.");
                            }

                            conn.setAutoCommit(true);

                        } catch (SQLException e) {
                            LOGGER.log(Level.SEVERE, "Error deleting product", e);
                            showAlert("Gagal menghapus barang: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    @FXML
    public void onLaporan() {
        try {
            // Membuat dialog untuk opsi laporan
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Laporan Penjualan");
            dialog.setHeaderText("Pilih jenis laporan dan periode");

            // Set up button types
            ButtonType generateButtonType = new ButtonType("Generate & Export", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

            // Set up radio buttons untuk jenis laporan
            Label jenisLabel = new Label("Jenis Laporan:");
            ToggleGroup jenisGroup = new ToggleGroup();
            RadioButton rbHarian = new RadioButton("Harian");
            rbHarian.setToggleGroup(jenisGroup);
            rbHarian.setSelected(true);
            RadioButton rbMingguan = new RadioButton("Mingguan");
            rbMingguan.setToggleGroup(jenisGroup);
            RadioButton rbBulanan = new RadioButton("Bulanan");
            rbBulanan.setToggleGroup(jenisGroup);

            // Set up DatePicker untuk tanggal
            Label tanggalLabel = new Label("Pilih Tanggal:");
            DatePicker datePicker = new DatePicker(LocalDate.now());

            // Setup layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(jenisLabel, 0, 0);
            grid.add(rbHarian, 1, 0);
            grid.add(rbMingguan, 2, 0);
            grid.add(rbBulanan, 3, 0);
            grid.add(tanggalLabel, 0, 1);
            grid.add(datePicker, 1, 1, 3, 1);

            dialog.getDialogPane().setContent(grid);

            // Get the window for the dialog
            if (tableBarang != null && tableBarang.getScene() != null && tableBarang.getScene().getWindow() != null) {
                dialog.initOwner(tableBarang.getScene().getWindow());
            }

            // Convert result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == generateButtonType) {
                    RadioButton selectedRB = (RadioButton) jenisGroup.getSelectedToggle();
                    String jenis = selectedRB.getText();
                    LocalDate tanggal = datePicker.getValue();
                    
                    return jenis + "," + tanggal.format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                try {
                    String[] parts = result.split(",");
                    String jenis = parts[0];
                    LocalDate tanggal = LocalDate.parse(parts[1]);
                    
                    // Generate laporan
                    generateLaporan(jenis, tanggal);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error parsing report parameters", e);
                    showAlert("Gagal membuat laporan: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening report dialog", e);
            showAlert("Gagal membuka dialog laporan: " + e.getMessage());
        }
    }
    
    private void generateLaporan(String jenis, LocalDate tanggal) {
        try {
            // Debug log
            LOGGER.info("Generating report: " + jenis + " for date " + tanggal);
            
            // Tentukan periode berdasarkan jenis dan tanggal
            LocalDate startDate;
            LocalDate endDate;
            String periodeLabel;
            
            switch (jenis) {
                case "Harian" -> {
                    startDate = tanggal;
                    endDate = tanggal;
                    periodeLabel = "Harian - " + tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LOGGER.info("Daily report period: " + startDate + " to " + endDate);
                }
                    
                case "Mingguan" -> {
                    // Cari hari Senin dalam minggu yang sama
                    startDate = tanggal.minusDays(tanggal.getDayOfWeek().getValue() - 1);
                    endDate = startDate.plusDays(6); // Minggu (7 hari dari Senin)
                    periodeLabel = "Mingguan - " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                  " s/d " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LOGGER.info("Weekly report period: " + startDate + " to " + endDate);
                }
                    
                case "Bulanan" -> {
                    startDate = tanggal.withDayOfMonth(1); // Hari pertama bulan
                    endDate = tanggal.withDayOfMonth(tanggal.lengthOfMonth()); // Hari terakhir bulan
                    periodeLabel = "Bulanan - " + tanggal.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                    LOGGER.info("Monthly report period: " + startDate + " to " + endDate);
                }
                    
                default -> {
                    LOGGER.warning("Invalid report type: " + jenis);
                    showAlert("Jenis laporan tidak valid: " + jenis);
                    return;
                }
            }
            
            // Ambil data penjualan dari database
            List<PenjualanData> penjualanList = getPenjualanData(startDate, endDate);
            LOGGER.info("Retrieved " + penjualanList.size() + " sales records");
            
            if (penjualanList.isEmpty()) {
                showAlert("Tidak ada data penjualan untuk periode " + periodeLabel);
                return;
            }
            
            // Export ke Excel
            exportToExcel(penjualanList, periodeLabel);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            showAlert("Gagal membuat laporan: " + e.getMessage());
        }
    }
    
    private List<PenjualanData> getPenjualanData(LocalDate startDate, LocalDate endDate) {
        List<PenjualanData> result = new ArrayList<>();
        
        try {
            // Format tanggal untuk query SQL
            String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            System.out.println("\n=== MEMULAI PENGAMBILAN DATA PENJUALAN ===");
            System.out.println("Periode: " + startDateStr + " s/d " + endDateStr);
            
            // Cek koneksi database terlebih dahulu
            System.out.println("Memeriksa koneksi database...");
            if (!DBConnection.testConnection()) {
                System.out.println("ERROR: Koneksi database gagal");
                showAlert("Tidak dapat terhubung ke database. Silakan periksa konfigurasi database.");
                return result;
            }
            System.out.println("Koneksi database berhasil.");
            
            // Query yang diperbaiki - menggunakan p.id bukan p.id_penjualan
            String query = "SELECT p.id, b.nama_barang, b.kategori, p.jumlah, p.harga_satuan, " +
                        "(p.jumlah * p.harga_satuan) as total, p.tanggal " +
                        "FROM penjualan p " +
                        "JOIN barang b ON p.id_barang = b.id_barang " +
                        "WHERE DATE(p.tanggal) BETWEEN ? AND ? " +
                        "ORDER BY p.tanggal";
            
            System.out.println("Query: " + query);
            System.out.println("Parameter: " + startDateStr + ", " + endDateStr);
            
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBConnection.getConnection();
                if (conn == null) {
                    System.out.println("ERROR: Koneksi database null");
                    showAlert("Tidak dapat terhubung ke database");
                    return result;
                }
                
                stmt = conn.prepareStatement(query);
                stmt.setString(1, startDateStr);
                stmt.setString(2, endDateStr);
                
                System.out.println("Menjalankan query...");
                rs = stmt.executeQuery();
                System.out.println("Query berhasil dijalankan.");
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt("id"); // Menggunakan "id" bukan "id_penjualan"
                    String namaBarang = rs.getString("nama_barang");
                    String kategori = rs.getString("kategori");
                    int jumlah = rs.getInt("jumlah");
                    double hargaSatuan = rs.getDouble("harga_satuan");
                    double total = rs.getDouble("total");
                    
                    // Debug output
                    System.out.println("Record #" + count + ": ID=" + id + 
                        ", Barang=" + namaBarang + ", Jumlah=" + jumlah + 
                        ", Total=" + total);
                    
                    PenjualanData data = new PenjualanData(
                        id, namaBarang, kategori, jumlah, hargaSatuan, total,
                        rs.getTimestamp("tanggal").toLocalDateTime()
                    );
                    result.add(data);
                }
                
                System.out.println("Total " + count + " record ditemukan");
                
                if (count == 0) {
                    System.out.println("PERINGATAN: Tidak ada data penjualan untuk periode yang dipilih");
                }
                
            } catch (SQLException e) {
                System.out.println("ERROR SQL: " + e.getMessage());
                e.printStackTrace(); // Untuk debugging
                showAlert("Error SQL: " + e.getMessage());
            } finally {
                // Close resources
                System.out.println("Menutup resources database...");
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    System.out.println("Error saat menutup resources: " + e.getMessage());
                }
            }
            
            System.out.println("=== SELESAI PENGAMBILAN DATA PENJUALAN ===\n");
            
        } catch (Exception e) {
            System.out.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace(); // Untuk debugging
            showAlert("Terjadi kesalahan yang tidak terduga: " + e.getMessage());
        }
        
        return result;
    }
    
    private void exportToExcel(List<PenjualanData> data, String periodeLabel) {
        if (data == null || data.isEmpty()) {
            showAlert("Tidak ada data untuk diekspor ke Excel");
            return;
        }
        
        try {
            System.out.println("Memulai proses ekspor ke Excel. Jumlah data: " + data.size());
            
            // Buat nama file berdasarkan periode dengan format yang diinginkan
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String fileName = "laporan-" + formattedDate + ".xlsx";
            
            // Gunakan direktori user untuk menyimpan file (bukan folder project)
            String userHome = System.getProperty("user.home");
            File downloadDir = new File(userHome + "/Downloads");
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
                System.out.println("Membuat direktori Downloads");
            }
            
            File file = new File(downloadDir, fileName);
            System.out.println("File laporan akan disimpan di: " + file.getAbsolutePath());
            
            // Gunakan try-with-resources untuk memastikan resource ditutup dengan benar
            try (
                Workbook workbook = new XSSFWorkbook();
                FileOutputStream outputStream = new FileOutputStream(file)
            ) {
                System.out.println("Membuat workbook Excel");
                
                // Buat sheet
                Sheet sheet = workbook.createSheet("Laporan Penjualan");
                
                // Buat style untuk header
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                
                // Buat header row
                Row headerRow = sheet.createRow(0);
                String[] columns = {"No", "ID Transaksi", "Nama Barang", "Kategori", "Jumlah", "Harga Satuan", "Total", "Tanggal"};
                
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                    sheet.setColumnWidth(i, 4000);
                }
                
                // Tambahkan baris judul
                Row titleRow = sheet.createRow(1);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Laporan Penjualan " + periodeLabel);
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);
                
                // Merge cells untuk judul
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
                
                // Buat baris data
                int rowNum = 3;
                double grandTotal = 0;
                
                System.out.println("Menulis " + data.size() + " baris data ke Excel");
                
                for (int i = 0; i < data.size(); i++) {
                    PenjualanData penjualan = data.get(i);
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(i + 1); // No
                    row.createCell(1).setCellValue(penjualan.getId());
                    row.createCell(2).setCellValue(penjualan.getNamaBarang());
                    row.createCell(3).setCellValue(penjualan.getKategori());
                    row.createCell(4).setCellValue(penjualan.getJumlah());
                    row.createCell(5).setCellValue(penjualan.getHargaSatuan());
                    row.createCell(6).setCellValue(penjualan.getTotal());
                    row.createCell(7).setCellValue(penjualan.getTanggal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    
                    grandTotal += penjualan.getTotal();
                }
                
                System.out.println("Grand total: " + grandTotal);
                
                // Tambahkan grand total
                Row totalRow = sheet.createRow(rowNum + 1);
                totalRow.createCell(0).setCellValue("GRAND TOTAL");
                totalRow.createCell(6).setCellValue(grandTotal);
                
                CellStyle totalStyle = workbook.createCellStyle();
                Font totalFont = workbook.createFont();
                totalFont.setBold(true);
                totalStyle.setFont(totalFont);
                totalRow.getCell(0).setCellStyle(totalStyle);
                totalRow.getCell(6).setCellStyle(totalStyle);
                
                // Merge cells untuk grand total
                sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 5));
                
                // Tambahkan statistik
                Row statRow1 = sheet.createRow(rowNum + 3);
                statRow1.createCell(0).setCellValue("Total Transaksi: " + data.size());
                
                Row statRow2 = sheet.createRow(rowNum + 4);
                statRow2.createCell(0).setCellValue("Periode: " + periodeLabel);
                
                Row statRow3 = sheet.createRow(rowNum + 5);
                statRow3.createCell(0).setCellValue("Dibuat pada: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                
                // Write to file
                System.out.println("Menyimpan workbook ke file: " + file.getAbsolutePath());
                workbook.write(outputStream);
                System.out.println("File Excel berhasil disimpan");
                showAlert("Laporan berhasil disimpan ke " + file.getAbsolutePath());
                
            } catch (IOException e) {
                System.err.println("Error Excel: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Error creating Excel file", e);
                showAlert("Gagal membuat file Excel: " + e.getMessage());
                e.printStackTrace(); // Untuk debugging
            }
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Unexpected error in exportToExcel", e);
            showAlert("Terjadi kesalahan saat membuat file Excel: " + e.getMessage());
            e.printStackTrace(); // Untuk debugging
        }
    }
    
    // Inner class untuk menyimpan data penjualan
    private static class PenjualanData {
        private final int id;
        private final String namaBarang;
        private final String kategori;
        private final int jumlah;
        private final double hargaSatuan;
        private final double total;
        private final LocalDateTime tanggal;
        
        public PenjualanData(int id, String namaBarang, String kategori, int jumlah, 
                            double hargaSatuan, double total, LocalDateTime tanggal) {
            this.id = id;
            this.namaBarang = namaBarang;
            this.kategori = kategori;
            this.jumlah = jumlah;
            this.hargaSatuan = hargaSatuan;
            this.total = total;
            this.tanggal = tanggal;
        }
        
        public int getId() { return id; }
        public String getNamaBarang() { return namaBarang; }
        public String getKategori() { return kategori; }
        public int getJumlah() { return jumlah; }
        public double getHargaSatuan() { return hargaSatuan; }
        public double getTotal() { return total; }
        public LocalDateTime getTanggal() { return tanggal; }
    }

    @FXML
    public void onDashboard() {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("/view/dashboard-view.fxml"));
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) tableBarang.getScene().getWindow();
            stage.setTitle("Dashboard DepiStore");
            stage.setScene(scene);
            
            // Set ukuran window yang konsisten dan tidak mepet
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening dashboard", e);
            showAlert("Gagal membuka dashboard: " + e.getMessage());
        }
    }

    /**
     * Method untuk generate laporan saat dipanggil dari controller lain.
     * Berguna untuk saat dipanggil dari DashboardController yang tidak memiliki akses ke tableBarang.
     */
    public static void generateLaporanFromOtherController(String jenis, LocalDate tanggal) {
        try {
            BarangController controller = new BarangController();
            controller.generateLaporan(jenis, tanggal);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report from other controller", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Gagal membuat laporan: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Method untuk membuat row factory dengan efek zebra hijau
    public Callback<TableView<Barang>, TableRow<Barang>> createTableRowFactory() {
        return tableView -> {
            TableRow<Barang> row = new TableRow<>();

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    int index = row.getIndex();
                    if (index % 2 == 0) {
                        // Baris genap: putih
                        row.setStyle("-fx-background-color: #ffffff; -fx-border-color: transparent;");
                    } else {
                        // Baris ganjil: biru muda
                        row.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: transparent;");
                    }
                } else {
                    row.setStyle("");
                }
            });
            
            // Hover effect
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered && row.getItem() != null) {
                    row.setStyle("-fx-background-color: #bbdefb; -fx-border-color: transparent;"); // biru hover
                } else if (row.getItem() != null) {
                    int index = row.getIndex();
                    if (index % 2 == 0) {
                        row.setStyle("-fx-background-color: #ffffff; -fx-border-color: transparent;");
                    } else {
                        row.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: transparent;");
                    }
                }
            });
            return row;
        };
    }

    private void setupCellFactories() {
        // Setup cell factory untuk kolom ID
        colId.setCellFactory(column -> new javafx.scene.control.TableCell<Barang, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(String.valueOf(id));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        // Setup cell factory untuk kolom Nama
        colNama.setCellFactory(column -> new javafx.scene.control.TableCell<Barang, String>() {
            @Override
            protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 8 0 8;");
                } else {
                    setText(nama);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 8 0 8;");
                }
            }
        });
        
        // Setup cell factory untuk kolom Kategori
        colKategori.setCellFactory(column -> new javafx.scene.control.TableCell<Barang, String>() {
            @Override
            protected void updateItem(String kategori, boolean empty) {
                super.updateItem(kategori, empty);
                if (empty || kategori == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(kategori);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        // Setup cell factory untuk kolom Harga
        colHarga.setCellFactory(column -> new javafx.scene.control.TableCell<Barang, Integer>() {
            @Override
            protected void updateItem(Integer harga, boolean empty) {
                super.updateItem(harga, empty);
                if (empty || harga == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText("Rp " + String.format("%,d", harga));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        // Setup cell factory untuk kolom Stok
        colStok.setCellFactory(column -> new javafx.scene.control.TableCell<Barang, Integer>() {
            @Override
            protected void updateItem(Integer stok, boolean empty) {
                super.updateItem(stok, empty);
                if (empty || stok == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(String.valueOf(stok));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        
        // Setup cell factory untuk kolom Tanggal
        colTanggal.setCellFactory(column -> new javafx.scene.control.TableCell<Barang, String>() {
            @Override
            protected void updateItem(String tanggal, boolean empty) {
                super.updateItem(tanggal, empty);
                if (empty || tanggal == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(tanggal);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }
}
