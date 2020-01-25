package org.bloomdex.client;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{
    private final Socket socket;
    private static byte[] data;
    private DataOutputStream dataOutputStream;

    /**
     * The constructor witch sets the socket and the dataOutputStream
     *
     * @param socket the socket witch allows the client to send data
     * @throws IOException throws an exception if something went wrong with getting the output stream from the socket
     */
    ClientThread(SSLSocket socket) throws IOException {
        this.socket = socket;
        dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * The function that sends the data
     */
    public void run() {
        try {
            while(!socket.isClosed()) {
                synchronized (this) {
                    this.wait();
                    dataOutputStream.writeInt(data.length); // write length of the message
                    dataOutputStream.write(data);
                    /*
                    for (int x = 0; x <= data.length - 1; x++) {
                        System.out.print((char)data[x]);
                    }
                    System.out.print("\n");
                     */
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("exception: %s%n", e.getMessage());
        }
    }

    /**
     * Sets the data witch will be sent
     *
     * @param data witch will be sent
     */
    static public void setData(byte[] data) {
        ClientThread.data = data;
    }
}
