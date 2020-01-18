package org.bloomdex;

import org.bloomdex.weatherdata.WeatherInstancesManager;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ConnectionThread implements Runnable{
    private Socket inputSocket;
    private Socket outputSocket;

    public ConnectionThread(Socket recvSocket){
        inputSocket = recvSocket;
    }

    @Override
    public void run() {
        try {
            readXML(inputSocket.getInputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readXML(InputStream inputStream) {
        try {
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            byte lineNum = -1;
            String[] currentMeasurement = new String[14];

            while ((line = dataIn.readLine()) != null) {
                if(line.contains("/MEASUREMENT")) {
                    lineNum = -1;

                    WeatherInstancesManager.updateInstances(currentMeasurement);
                    currentMeasurement = new String[14];

                    continue;
                }
                else if(line.contains("MEASUREMENT")) {
                    lineNum = 0;
                    continue;
                }
                else if(line.contains("/WEATHERDATA")) {
                    lineNum = -1;
                    continue;
                }

                if(lineNum != -1) {
                    currentMeasurement[lineNum] = line.substring(line.indexOf(">") + 1, line.indexOf("/") - 1);
                    lineNum += 1;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
