package org.bloomdex;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
    private static byte responsibilityByte = 0;

    public static void StartConnection() {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(801);
        CounterManager counterManager = null;

        try {
            ServerSocket serverSocket = new ServerSocket(7789);

            while(!serverSocket.isClosed()) {
                if(counterManager == null)
                    counterManager = new CounterManager(10);
                else
                    counterManager.reset();

                threadExecutor.execute(new ConnectionThread(serverSocket.accept()));
            }

            threadExecutor.shutdown();
        } catch (IOException e) {
            System.err.println("Fout met gegeven server socket.");
        }
    }

    public static void flipResponsibilityByte() {
        if(responsibilityByte == 0)
            responsibilityByte = 1;
        else
            responsibilityByte = 0;

        System.out.println("ConnectionManager: Responsibility byte is now " + responsibilityByte);
    }

    public static byte getResponsibilityByte() { return responsibilityByte; }
}
