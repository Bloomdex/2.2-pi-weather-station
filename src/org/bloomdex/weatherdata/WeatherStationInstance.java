package org.bloomdex.weatherdata;

import java.util.ArrayList;
import java.util.Arrays;

public class WeatherStationInstance {
    public String stn;


    // Weather data array lists
    private ArrayList<Object[]> currentWeatherMeasurements = new ArrayList<>();

    public WeatherStationInstance(String stn) {
        this.stn = stn;
    }

    public void addWeatherMeasurement(String[] measurementEntry) {
        //byte measureArrayIndex = (byte)(index - 3);
        Object[] currentWeatherMeasurement = new Object[14];

        for(byte i = 0; i < measurementEntry.length; i++) {
            switch (i) {
                case 0: case 1: case 2:
                    currentWeatherMeasurement[i] = measurementEntry[i];
                    break;
                case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 12:
                    try {
                        currentWeatherMeasurement[i] = Float.parseFloat(measurementEntry[i]);
                        addToWeatherDataBuffers(i, currentWeatherMeasurement[i]);
                    }
                    catch(NumberFormatException e) {
                        break;
                    }

                    break;
                case 11:
                    try {
                        currentWeatherMeasurement[i] = Byte.parseByte(measurementEntry[i], 2);
                    }
                    catch (NumberFormatException e) {
                        break;
                    }

                    break;
                case 13:
                    try {
                        currentWeatherMeasurement[i] = Short.valueOf(measurementEntry[i]);
                        addToWeatherDataBuffers(i, currentWeatherMeasurement[i]);
                    }
                    catch (NumberFormatException e) {
                        break;
                    }

                    break;
            }
        }

        //System.out.println(Arrays.toString(currentWeatherMeasurement) + "\t\t\t" + currentWeatherMeasurements.size());
        currentWeatherMeasurements.add(currentWeatherMeasurement);
    }


    // Weather data buffer arrays
    final static private byte WEATHER_DATA_ARRAY_SIZE = 30;

    private Object[][] weatherDataBufferArrs = new Object[10][WEATHER_DATA_ARRAY_SIZE];
    private byte[] weatherDataBufferPointers = new byte[10];

    private void addToWeatherDataBuffers(byte xmlIndex, Object data) {
        // Get the buffer index
        /* XML to Buffer array conversion table
        XML Line num    Buffer index num
        3 t/m 10  	    XML num - 3
        12 en 13        XML num - 4
         */
        byte bufferIndex = (byte)(xmlIndex - 3);

        if(xmlIndex > 10)
            bufferIndex -= 1; // Subtract an extra value according to the XML to Buffer array conversion table


        // Update the current buffer
        byte currentWeatherDataBufferPointer = weatherDataBufferPointers[bufferIndex];
        weatherDataBufferArrs[bufferIndex][currentWeatherDataBufferPointer] = data;
        /*System.out.println("Buffer: " + Arrays.toString(weatherDataBufferArrs[bufferIndex])
                + "\n\tWith buffer index: " + bufferIndex
                +"\n\tWith pointer: " + currentWeatherDataBufferPointer
                + "\n\tWith data: " + data);*/

        // Set the new buffer pointer
        currentWeatherDataBufferPointer += 1;

        if (currentWeatherDataBufferPointer == WEATHER_DATA_ARRAY_SIZE)
            weatherDataBufferPointers[bufferIndex] = 0;
        else
            weatherDataBufferPointers[bufferIndex] = currentWeatherDataBufferPointer;
    }
}
