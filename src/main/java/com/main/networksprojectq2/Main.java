package com.main.networksprojectq2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static java.lang.System.exit;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Controller.currentStage = stage;
        stage.setTitle("Messenger!");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            try {

                Functions.logout();
                exit(0);
            } catch (Exception e) {
//                throw new RuntimeException(e);
                exit(0);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}