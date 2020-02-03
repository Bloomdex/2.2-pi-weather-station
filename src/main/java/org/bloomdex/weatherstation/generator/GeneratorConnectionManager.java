package org.bloomdex.weatherstation.generator;

import org.bloomdex.client.ClientManager;
import org.bloomdex.weatherstation.weatherdata.WeatherDataManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneratorConnectionManager {
    /**
     * Starts a connection to the generator program and created a thread for every connection opened.
     */
    public static void StartConnection() {
        ExecutorService threadExecutor = Executors.newFixedThreadPool(801);

        try {
            ServerSocket serverSocket = new ServerSocket(7789);

            while(!serverSocket.isClosed()) {
                threadExecutor.execute(new GeneratorConnectionThread(serverSocket.accept()));
            }
        }
        catch (IOException e) {
            System.err.println("Problem with given socket: .");
            e.printStackTrace();
        }
        finally {
            try {
                ClientManager.stopServerFromAPI();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            threadExecutor.shutdown();
        }
    }

    /**
     * Handle the data gathered by the weather instances by sending it to the server.
     */
    public static void handleData() {
        System.out.println("Amount of measurements in the past 10 seconds: " + WeatherDataManager.getMeasurementSetAmount());
        System.out.println("Amount of bytes in the past 10 seconds: " + WeatherDataManager.getParsedMeasurementSets().size());
        System.out.println("Amount of measurements in total: " + WeatherDataManager.getMeasurementSetMaxAmount());

        if (WeatherDataManager.getMeasurementSetAmount() != 0) {
            ClientManager.setData(WeatherDataManager.getParsedMeasurementSetsPrim());
            WeatherDataManager.resetData();
        }
    }
}
