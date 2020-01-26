package org.bloomdex.weatherstation.generator;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneratorConnectionManager {
    private static byte responsibilityByte = 0;

    /**
     * Starts a connection to the generator program and created a thread for every connection opened.
     */
    public static void StartConnection() {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(801);

        try {
            ServerSocket serverSocket = new ServerSocket(7789);

            while(!serverSocket.isClosed()) {
                threadExecutor.execute(new GeneratorConnectionThread(serverSocket.accept(), responsibilityByte));
            }

            threadExecutor.shutdown();
        } catch (IOException e) {
            System.err.println("Problem with given socket: .");
            e.printStackTrace();
        }
    }

    /**
     * Flips the responsibility byte between 0 and 1 for threads to read.
     */
    public static void flipResponsibilityByte() {
        System.out.println(allMeasurements.size());
        allMeasurements.clear();

        if(responsibilityByte == 0)
            responsibilityByte = 1;
        else
            responsibilityByte = 0;

        System.out.println("GeneratorConnectionManager: Responsibility byte is now " + responsibilityByte);
    }

    /**
     * @return the responsibility byte.
     */
    static byte getResponsibilityByte() { return responsibilityByte; }


    private static final ArrayList<Byte> allMeasurements = new ArrayList<>();

    static void storeMeasurements(ArrayList<Byte> measurements) {
        synchronized (allMeasurements) {
            allMeasurements.addAll(measurements);
        }
    }
}
