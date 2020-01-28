package org.bloomdex.client;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{
    private final Socket socket;
    private byte[] data;
    private DataOutputStream dataOutputStream;

    /**
     * The constructor which sets the socket and dataOutputStream
     * @param socket the socket that data is being send towards
     * @throws IOException throws an exception if something went wrong with getting the output stream from the socket
     */
    ClientThread(SSLSocket socket) throws IOException {
        this.socket = socket;
        dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * Sends the data to the server
     */
    @Override
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
        }
        catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    /**
     * Sets the data which will be sent
     * @param data which will be sent
     */
    public synchronized void setData(byte[] data) {
        this.data = data;
    }
}
