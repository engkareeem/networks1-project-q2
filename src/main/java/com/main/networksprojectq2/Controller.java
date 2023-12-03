package com.main.networksprojectq2;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    TextField usernameField,passwordField;
    @FXML
    Button loginLogoutButton;

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



        VBox vBox = new VBox();
        vBox.getStyleClass().add("chatVBox");
        vBox.setId("chatVBox");
        chatPane.setContent(vBox);
        Functions.getInterfaces();
        ReceiverUDP.init();
    }



    public void loginLogoutButtonClicked() {
        // TODO: Login Logout Button

        // if the login failed or the logout, return the function...

        if(isLogin) {
            isLogin = false;
            loginLogoutButton.setText("Login");
        } else {
            isLogin = true;
            loginLogoutButton.setText("Logout");
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