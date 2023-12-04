package com.main.networksprojectq2;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReceiverUDP {
    private static int listeningPort;
    private static DatagramSocket datagramSocket;

    private static Thread mainThread;

    static {
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init(){
        mainThread = new Thread(ReceiverUDP::receiveUDP);
        mainThread.start();
    }

    public static void updatePort(int port){
        if(port == listeningPort) return;
        listeningPort = port;
        try {
            mainThread.interrupt();
            datagramSocket.close();
            datagramSocket = new DatagramSocket(listeningPort);
            init();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }


    public static void receiveUDP() {
        DatagramPacket packet = null;
        byte[] receiveArray = new byte[65535];
        while (!Thread.interrupted()) {
            packet = new DatagramPacket(receiveArray, receiveArray.length);

            try {
                datagramSocket.receive(packet);
            } catch (IOException e) {
                continue;
//                throw new RuntimeException(e);
            }
            String msg = new String(packet.getData()).trim();
            System.out.println(msg);
            String username = msg.split("@")[0];
            String ip = msg.split("@")[1];
            String port = msg.split("@")[2];

            Platform.runLater(() -> {
                TextField ipField = (TextField) Controller.currentStage.getScene().lookup("#remoteIPField");
                TextField portField = (TextField) Controller.currentStage.getScene().lookup("#remotePortField");
                ipField.setText(ip);
                portField.setText(port);
            });

            msg = msg.substring(msg.indexOf('@', msg.indexOf('@', msg.indexOf('@') + 1) + 1) + 1);
            if(msg.contains("CMD")){
                processCommand(msg, ip, port);
                continue;
            }
            String msgId = msg.split("@")[0];
            String msgBody = msg.split("@")[1];

            Platform.runLater(() -> {
                Functions.changeStatus("Received a message from " + username + ":", ip, port);
                Functions.addMessage(username + ": " + msgBody, false, msgId);

            });
            receiveArray = new byte[65535];

        }
    }

    public static void processCommand(String cmd, String ip, String port) {
        if(cmd.contains("delete@")) {

            String id = cmd.split("@")[0];
            Functions.deleteMessage(id);
            Functions.changeStatus("A Message deleted by", ip, port);
        }else if(cmd.contains("deleteAll@")){

            Functions.deleteAllMessages();
            Functions.changeStatus("All messages deleted by", ip, port);

        }
    }



}