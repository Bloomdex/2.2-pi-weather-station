package org.bloomdex.weatherstation.connection.generator;

import org.bloomdex.weatherstation.weatherdata.WeatherInstancesManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class GeneratorConnectionThread implements Runnable{
    private Socket inputSocket;
    private byte responsibilityByte;
    private WeatherInstancesManager weatherInstancesManager;

    /**
     * Creates a generator connection thread.
     * @param recvSocket the socket that data is received from.
     * @param connectionManagerResponsibilityByte the current responsibility byte in the connection manager.
     */
    GeneratorConnectionThread(Socket recvSocket, byte connectionManagerResponsibilityByte){
        inputSocket = recvSocket;
        responsibilityByte = connectionManagerResponsibilityByte;

        weatherInstancesManager = new WeatherInstancesManager();
    }

    /**
     * Reads the XML data that is received from the input socket and creates or updates a weather manager
     * with measurements that are placed in an array as the XML is being read.
     */
    @Override
    public void run() {
        try {
            // Read XML data from the input socket using a buffered reader
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(inputSocket.getInputStream()));

            String line;
            byte lineNum = -1;
            String[] currentMeasurement = new String[14];

            // Loop through each line in the received data
            while ((line = dataIn.readLine()) != null) {
                if(line.contains("/MEASUREMENT")) {
                    // Stop reading measurements
                    lineNum = -1;

                    // Send the past read measurements to the matching instance
                    weatherInstancesManager.updateInstances(currentMeasurement);
                    currentMeasurement = new String[14];

                    continue;
                }
                else if(line.contains("MEASUREMENT")) {
                    // Start reading measurements
                    lineNum = 0;
                    continue;
                }
                else if(line.contains("/WEATHERDATA")) {
                    // Stop reading measurements
                    lineNum = -1;
                    checkThreadActions();
                    continue;
                }

                // If measurements are being read, add the measurement as a String to the currentMeasurement array
                if(lineNum != -1) {
                    currentMeasurement[lineNum] = line.substring(line.indexOf(">") + 1, line.indexOf("/") - 1);
                    lineNum += 1;
                }
            }

            end();
        }
        catch (IOException e) {
            e.printStackTrace();
            end();
        }
    }

    /**
     * Handles stoppage of this thread.
     */
    private void end() {
        // Stop this thread
        Thread.currentThread().interrupt();
    }

    /**
     * Checks if thread has to do any actions based on the responsibility byte from the manager
     * such as clearing buffers and sending data to the server.
     */
    private void checkThreadActions() {
        byte currentResponsibilityByte = GeneratorConnectionManager.getResponsibilityByte();

        if(responsibilityByte != currentResponsibilityByte) {
            responsibilityByte = currentResponsibilityByte;
            weatherInstancesManager.handleData();
        }
    }
}
