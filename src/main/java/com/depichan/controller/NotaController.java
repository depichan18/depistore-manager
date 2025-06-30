package com.depichan.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;

public class NotaController {

    @FXML private Label labelNota;
    @FXML private Button btnSavePdf;

    private Stage stage;
    private String notaText;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setNotaData(String namaBarang, int jumlah, double harga, double total, String waktu) {
        notaText = String.format("""
                =====================
                NOTA PENJUALAN
                =====================
                Nama Barang : %s
                Jumlah      : %d
                Harga Satuan: Rp %.2f
                ---------------------
                Total Bayar : Rp %.2f
                Tanggal     : %s
                """, namaBarang, jumlah, harga, total, waktu);

        labelNota.setText(notaText);
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    @FXML
    private void onSaveAsPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Nota sebagai PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("nota_penjualan.pdf");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                Document document = new Document();
                PdfWriter.getInstance(document, fos);
                document.open();
                document.add(new Paragraph(notaText, new Font(Font.COURIER, 12)));
                document.close();

                showAlert("Berhasil", "Nota berhasil disimpan sebagai PDF.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Gagal", "Gagal menyimpan PDF: " + e.getMessage());
            }
        }
    }
    public void saveNotaAsPDFToFile(File file) {
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                Document document = new Document();
                PdfWriter.getInstance(document, fos);
                document.open();
                document.add(new Paragraph(notaText, new Font(Font.COURIER, 12)));
                document.close();
                System.out.println("Nota PDF berhasil disimpan ke: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
