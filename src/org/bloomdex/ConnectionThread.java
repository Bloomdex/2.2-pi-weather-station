package org.bloomdex;

import java.io.*;
import java.net.Socket;

public class ConnectionThread implements Runnable{
    private Socket inputSocket;
    private Socket outputSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ConnectionThread(Socket recvSocket){
        try {
            inputSocket = recvSocket;

            dataInputStream = new DataInputStream(inputSocket.getInputStream());
            dataOutputStream = new DataOutputStream(inputSocket.getOutputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Thread: " + Thread.currentThread().getId()
                + "\n" + dataInputStream
                + "\n" + dataOutputStream
                + "\n------------------------------------------------"
        );
    }
}
