package com.depichan;

import java.io.IOException;

import com.depichan.db.DBConnection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        AnchorPane root = FXMLLoader.load(getClass().getResource("/view/dashboard-view.fxml"));
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("Dashboard - DepiStore Manager");
        primaryStage.getIcons().add(new Image(getClass().getResource("/img/icon.png").toString()));
        primaryStage.setScene(scene);
        
        // Set ukuran window yang konsisten dan tidak mepet
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Test koneksi sebelum launch
        DBConnection.getConnection();
        launch(args);
    }
}