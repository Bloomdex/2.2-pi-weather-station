package org.bloomdex;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
    ExecutorService theadExecutor = Executors.newFixedThreadPool(400);

    public ConnectionManager() {
        try {
            ServerSocket serverSocket = new ServerSocket(7789);

            while(true) {
                theadExecutor.execute(new ConnectionThread(serverSocket.accept()));
            }
        } catch (IOException e) {
            System.err.println("Fout met gegeven server socket.");
        }
    }
}
