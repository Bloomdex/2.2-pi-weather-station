package org.bloomdex;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
    public static void StartConnection() {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(800);

        try {
            ServerSocket serverSocket = new ServerSocket(7789);

            while(!serverSocket.isClosed()) {
                threadExecutor.execute(new ConnectionThread(serverSocket.accept()));
                //new ConnectionThread(serverSocket.accept());
            }

            threadExecutor.shutdown();
        } catch (IOException e) {
            System.err.println("Fout met gegeven server socket.");
        }
    }
}
