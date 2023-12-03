package com.main.networksprojectq2;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

public class Functions {

    public static void addMessage(String text, boolean sent, String id) {
        Long timestamp = new Date().getTime();
        VBox chatVBox = (VBox) Controller.currentStage.getScene().lookup("#chatVBox");
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a"); // DD for date, E for day name
        Pane messagePane = new Pane();
        HBox messageHBox = new HBox();
        StackPane textStackPane = new StackPane();
        StackPane dateStackPane = new StackPane();
        Text messageText = new Text();
        Text messageDate = new Text();
        Button deleteMessageButton = new Button();


        messagePane.setId("message" + id);


        deleteMessageButton.setOnAction(event -> {
            String ip = ((TextField) Controller.currentStage.getScene().lookup("#remoteIPField")).getText();
            String port = ((TextField) Controller.currentStage.getScene().lookup("#remotePortField")).getText();

            sendUDP("CMD@delete@", ip, Integer.parseInt(port),id);
            Functions.deleteMessage(id);
        });

        deleteMessageButton.getStyleClass().add("deleteMessageButton");

        messageDate.setText(dateFormat.format(timestamp));
        messageDate.getStyleClass().add("messageDateText");

        dateStackPane.getStyleClass().add("messageDatePane");
        dateStackPane.getChildren().add(messageDate);

        messageText.setText(text);
        messageText.setWrappingWidth(sent? 185: 210);
        messageText.getStyleClass().add("messageContentText");

        textStackPane.getStyleClass().add("messageContentPane");
        messagePane.getStyleClass().add(sent ? "sentMessagePane":"receivedMessagePane");
        textStackPane.getChildren().add(messageText);

        messageHBox.getChildren().add(sent ? textStackPane:dateStackPane);
        if(sent) messageHBox.getChildren().add(deleteMessageButton);
        messageHBox.getChildren().add(sent ? dateStackPane:textStackPane);

        messagePane.getChildren().add(messageHBox);
        chatVBox.getChildren().add(messagePane);
    }
    public static void changeStatus(String text, String ip, String port) {
        TextArea textArea = (TextArea) Controller.currentStage.getScene().lookup("#statusArea");
        textArea.setText(text + " IP = " + ip + ", Port = " + port);
    }
    public static void deleteMessage(String msgID) {
        try {
            VBox vBox = (VBox) (Controller.currentStage.getScene().lookup("#chatVBox"));
            Pane messagePane = (Pane) vBox.lookup("#message" + msgID);
            Platform.runLater(() -> {
                vBox.getChildren().remove(messagePane);
            });
        } catch (Exception e) {
            System.out.println("Error occurred while deleting message pane ");
            e.printStackTrace();
        }
    }

    public static void deleteAllMessages() {
        Platform.runLater(() -> {
            VBox vbox = (VBox) Controller.currentStage.getScene().lookup("#chatVBox");
            vbox.getChildren().clear();
        });

    }

    public static ArrayList<String> getInterfaces(){
        ArrayList<String> interfaces = new ArrayList<>();

        try {

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();


            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if(networkInterface.getInetAddresses().hasMoreElements()){
                    String interfaceAddress = networkInterface.getInetAddresses().nextElement().getHostAddress();
                    String name = networkInterface.getName().replaceAll("wlan\\d*", "Wi-Fi").replaceAll("lo\\d*", "LocalHost").replaceAll("eth\\d*", "Ethernet");
                    if(interfaceAddress.matches("\\d+.\\d+.\\d+.\\d+")){
                        interfaces.add(name + ": " + interfaceAddress);
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return interfaces;
    }

    public static void sendUDP(String message,String ip, int port, String id) {

        try {
            String localIp = ((TextField) Controller.currentStage.getScene().lookup("#localIPField")).getText();
            String localPort = ((TextField) Controller.currentStage.getScene().lookup("#localPortField")).getText();
            message =  localIp + "@" + localPort + "@" + id + "@" + message;

            DatagramSocket ds = new DatagramSocket();
            byte[] buf;
            buf = message.getBytes();
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
            ds.send(DpSend);
            if(message.contains("CMD")){
                Functions.changeStatus("Sent a command to", ip, String.valueOf(port));
            }else {
                Functions.changeStatus("Sent a message to", ip, String.valueOf(port));

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}