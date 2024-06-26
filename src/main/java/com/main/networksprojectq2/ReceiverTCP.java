package com.main.networksprojectq2;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverTCP {
    public static String[] onlineUsers;
    private static ServerSocket serverSocket;

    private static int listeningPort;

    private static boolean keepListening = true;

    private static Thread onlineUsersThread;


    public static void init(int port){
        try {
            listeningPort = port;
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static void stopListening(){
        keepListening = false;
        try {
            onlineUsersThread.interrupt();
            serverSocket.close();
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    public static void onlineUsersReceiver(int port) {
        keepListening = true;
        if(serverSocket.isClosed()) init(port);
        onlineUsersThread = new Thread(() -> {
            while (keepListening){
                Socket socket;

                try {
                    socket = serverSocket.accept();
                    new Thread(() -> {
                        try {

                            DataInputStream inputStream = new DataInputStream(
                                    new BufferedInputStream(socket.getInputStream()));
                            String msg = inputStream.readUTF();
                            System.out.println(msg);

                            if(msg.split("@")[1].equalsIgnoreCase("notify")) {
                                onlineUsers = msg.substring(msg.indexOf('@', msg.indexOf('@') + 1) + 1).split("@");
                                Controller.updateOnlineUsers();

                            }

                            inputStream.close();
                            socket.close();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    }).start();
                } catch (IOException e) {
//                throw new RuntimeException(e);
                }
            }
        });

        onlineUsersThread.start();


    }

    public static boolean receiveTCP(int port) {
        if(listeningPort != port || serverSocket.isClosed()) init(port);
        Socket socket;
        boolean successLoggedIn = false;
        try {
            socket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            String msg = inputStream.readUTF();

            if(msg.split("@")[1].equalsIgnoreCase("notify")) {
                onlineUsers = msg.substring(msg.indexOf('@', msg.indexOf('@') + 1) + 1).split("@");
                Controller.updateOnlineUsers();

            } else if(msg.split("@")[1].equalsIgnoreCase("error")) {
                String errorMsg = msg.split("@")[2];
                Functions.setStatus(errorMsg);
            } else if(msg.split("@")[1].equalsIgnoreCase("success")){
                String successMsg = msg.split("@")[2];
                Functions.setStatus(successMsg);
                successLoggedIn = true;
            }


            inputStream.close();
            socket.close();



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return successLoggedIn;
    }

    public static void processCommand(String cmd, String ip, String port) {
        if(cmd.contains("delete@")) {
            // TODO: delete a msg
            String id = cmd.split("@")[0];
//            Functions.deleteMessage(id);
//            Functions.changeStatus("A Message deleted by", ip, port);
        }else if(cmd.contains("deleteAll@")){
            //TODO: delete all user message
//            Functions.deleteAllMessages();
//            Functions.changeStatus("All messages deleted by", ip, port);

        }
    }



}