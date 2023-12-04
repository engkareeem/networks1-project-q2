package com.main.networksprojectq2;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    ScrollPane chatPane;
    @FXML
    TextField chatField;
    @FXML
    TextField remoteIPField,remotePortField;
    @FXML
    TextField localIPField,localPortField;
    @FXML
    TextField TCPServerIPField,TCPServerPortField;
    @FXML
    ComboBox<String> interfacesComboBox;
    @FXML
    TextArea statusArea;
    @FXML
    TextField usernameField, passwordField;
    @FXML
    Button loginLogoutButton, sendToAllButton;

    boolean isLogin = false;

    public static Stage currentStage;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        /* Events */
        TextField []numericFields = {remotePortField,TCPServerPortField};
        for(TextField tf:numericFields) {
            tf.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if(newValue.length() > 4) {
                    tf.setText(oldValue);
                }
            });
        }
        localPortField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") || newValue.length() > 5) {
                localPortField.setText(newValue.replaceAll("[^\\d]", ""));
                if(newValue.length() > 4) {
                    localPortField.setText(oldValue);
                }

            } else if(!newValue.isEmpty()) {
                ReceiverUDP.updatePort(Integer.parseInt(newValue));
            }
        });

        /*      ComboBox initialize   */

        for(String inter: Functions.getInterfaces()) {
            interfacesComboBox.getItems().add(inter);
        }

        remoteIPField.setDisable(true);
        remotePortField.setDisable(true);
        localIPField.setDisable(true);


        VBox vBox = new VBox();
        vBox.getStyleClass().add("chatVBox");
        vBox.setId("chatVBox");
        chatPane.setContent(vBox);
        Functions.getInterfaces();
        ReceiverUDP.init();
    }



    public static void updateOnlineUsers() {
        Platform.runLater(() -> {
            VBox vbox = (VBox) currentStage.getScene().lookup("#onlineUsersVBox");
            TextField ipField = (TextField) Controller.currentStage.getScene().lookup("#remoteIPField");
            TextField portField = (TextField) Controller.currentStage.getScene().lookup("#remotePortField");

            vbox.getChildren().clear();
            for(String user: ReceiverTCP.onlineUsers) {
                Label label = new Label(user);
                label.getStyleClass().add("onlineUserLabel");
                String ip = user.split(":")[1];
                String port = user.split(":")[2];
                label.setOnMouseClicked(mouseEvent -> {
                    if(!ipField.getText().equals(ip) || !portField.getText().equals(port)) {
                        ipField.setText(ip);
                        portField.setText(port);
                        Functions.deleteAllMessages();
                    }
                });
                vbox.getChildren().add(label);
            }
        });

    }

    public void loginLogoutButtonClicked() {

        if(isLogin) {
            loginLogoutButton.setDisable(true);
            Functions.logout();
            isLogin = false;
            loginLogoutButton.setText("Login");
            localIPField.setDisable(false);
            localPortField.setDisable(false);
            usernameField.setDisable(false);
            passwordField.setDisable(false);
            interfacesComboBox.setDisable(false);
            loginLogoutButton.setDisable(false);
            remoteIPField.clear();
            remotePortField.clear();
        } else {
            loginLogoutButton.setDisable(true);
            new Thread(() -> {

                    if(Functions.login(usernameField.getText(), passwordField.getText(), TCPServerIPField.getText(), TCPServerPortField.getText())){
                        isLogin = true;
                        Platform.runLater(() -> {
                        localIPField.setDisable(true);
                        localPortField.setDisable(true);
                        usernameField.setDisable(true);
                        passwordField.setDisable(true);
                        interfacesComboBox.setDisable(true);
                        loginLogoutButton.setText("Logout");
                        });

                    }
                Platform.runLater(() -> {

                    loginLogoutButton.setDisable(false);
                });

            }).start();

        }
    }




    public void sendButtonClicked() {
        if(!chatField.getText().isEmpty() && !remoteIPField.getText().isEmpty() && !remotePortField.getText().isEmpty()) {
            String message = chatField.getText();
            String ip = remoteIPField.getText();
            int port = Integer.parseInt(remotePortField.getText());
            new Thread(() -> {
                String id = generateMessageID();
                Functions.sendUDP(message, ip, port, id);
                chatField.clear();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        Functions.addMessage(message,true, id); // TODO: There is an id
                    }
                });
            }).start();
        }
    }

    public void sendToAllButtonClicked() {
        // TODO: Send to all
    }

    String generateMessageID() {
        Long timestamp = new Date().getTime();
        return timestamp.toString();
    }


    public void interfacesComboBoxOnChanged() {
        String selected = interfacesComboBox.getSelectionModel().getSelectedItem();
        String ip = selected.split(": ")[1];
        localIPField.setText(ip);
    }

    public void deleteAllButtonClicked(ActionEvent e) {
        if(!remoteIPField.getText().isEmpty() && !remotePortField.getText().isEmpty()) {
            Functions.deleteAllMessages();
            String ip = ((TextField) Controller.currentStage.getScene().lookup("#remoteIPField")).getText();
            String port = ((TextField) Controller.currentStage.getScene().lookup("#remotePortField")).getText();
            Functions.sendUDP("CMD@deleteAll@", ip, Integer.parseInt(port), "");
        }
    }




}