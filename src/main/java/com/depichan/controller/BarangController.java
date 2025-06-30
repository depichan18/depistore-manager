package com.depichan.controller;

import com.depichan.db.DBConnection;
import com.depichan.model.Barang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BarangController {

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

        cbSortBy.setItems(FXCollections.observableArrayList("Nama", "Harga", "Stok"));
        cbSortOrder.setItems(FXCollections.observableArrayList("ASC", "DESC"));

        cbSortBy.setValue("Nama");
        cbSortOrder.setValue("ASC");

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData());
        cbSortBy.setOnAction(e -> loadData());
        cbSortOrder.setOnAction(e -> loadData());

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
            e.printStackTrace();
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
                new File("nota").mkdirs(); // pastikan folder ada
                String fileName = "nota/nota-" + System.currentTimeMillis() + ".pdf";
                File pdfFile = new File(fileName);
                notaController.saveNotaAsPDFToFile(pdfFile);

            } catch (NumberFormatException e) {
                showAlert("Masukkan angka yang valid.");
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                showAlert("Gagal menyimpan penjualan atau menampilkan nota.");
            }
        });
    }

    private void showAlert(String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }
}
