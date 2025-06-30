package com.depichan;

import com.depichan.db.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        AnchorPane root = FXMLLoader.load(getClass().getResource("/view/dashboard-view.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Dashboard Toko Kelontong");
        primaryStage.getIcons().add(new Image(getClass().getResource("/img/icon.png").toString()));
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Test koneksi sebelum launch
        DBConnection.getConnection();
        launch(args);
    }
}