package org.bloomdex;

import org.bloomdex.weatherdata.WeatherInstancesManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionThread implements Runnable{
    private Socket inputSocket;
    private byte responsibilityByte;
    private WeatherInstancesManager weatherInstancesManager;

    public ConnectionThread(Socket recvSocket, byte connectionManagerResponsibilityByte){
        inputSocket = recvSocket;
        responsibilityByte = connectionManagerResponsibilityByte;

        weatherInstancesManager = new WeatherInstancesManager();
    }

    @Override
    public void run() {
        try {
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(inputSocket.getInputStream()));

            String line;
            byte lineNum = -1;
            String[] currentMeasurement = new String[14];

            while ((line = dataIn.readLine()) != null) {
                if(line.contains("/MEASUREMENT")) {
                    lineNum = -1;

                    weatherInstancesManager.updateInstances(currentMeasurement);
                    currentMeasurement = new String[14];

                    continue;
                }
                else if(line.contains("MEASUREMENT")) {
                    lineNum = 0;
                    continue;
                }
                else if(line.contains("/WEATHERDATA")) {
                    lineNum = -1;
                    checkThreadActions();
                    continue;
                }

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

    private void end() {
        Thread.currentThread().interrupt();
    }

    private void checkThreadActions() {
        byte currentResponsibilityByte = ConnectionManager.getResponsibilityByte();

        if(responsibilityByte != currentResponsibilityByte) {
            if(Thread.currentThread().getName().equals("pool-1-thread-1")) {
                System.out.println("THREAD 1 IS RESTARTING");
            }

            responsibilityByte = currentResponsibilityByte;
            weatherInstancesManager.handleData();
        }
    }
}
