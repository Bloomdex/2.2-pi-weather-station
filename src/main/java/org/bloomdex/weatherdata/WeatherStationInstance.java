package org.bloomdex.weatherdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherStationInstance {
    private String stn;
    private SimpleDateFormat simpleDateFormat;


    // Weather data array lists
    private ArrayList<Object[]> currentWeatherMeasurementsArr = new ArrayList<>();

    WeatherStationInstance(String stn) {
        this.stn = stn;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    void addWeatherMeasurement(String[] measurementEntry) {
        Object[] currentWeatherMeasurement = new Object[14];

        String dateString = "";
        byte corrOffset = 0;
        byte corrIndex;
        byte discardMeasurement = 0;

        for(byte i = 0; i < measurementEntry.length; i++) {
            corrIndex = (byte)(i - corrOffset);

            switch (i) {
                case 0:
                    currentWeatherMeasurement[corrIndex] = Integer.parseInt(measurementEntry[i]);
                    break;
                case 1: case 2:
                    if(i == 1) {
                        dateString = measurementEntry[i];
                    }
                    else {
                        try{
                            Date parsedDateTime = simpleDateFormat.parse(dateString + " " + measurementEntry[i]);
                            currentWeatherMeasurement[corrIndex - 1] = (int)(parsedDateTime.getTime()/1000);
                        }
                        catch(ParseException e) {
                            discardMeasurement = 1;
                        }

                        corrOffset = 1;
                    }

                    break;
                case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 12:
                    try {
                        currentWeatherMeasurement[corrIndex] = Float.parseFloat(measurementEntry[i]);
                        addToWeatherDataBuffers(corrIndex, currentWeatherMeasurement[corrIndex]);
                    }
                    catch(NumberFormatException e) {
                        float correctedMeasurement = WeatherMaths.calcLWMA(getWeatherDataBuffer(corrIndex), WeatherMaths.DataType.FLOAT);

                        if (correctedMeasurement != Float.MIN_VALUE) {
                            currentWeatherMeasurement[corrIndex] = correctedMeasurement;
                            addToWeatherDataBuffers(corrIndex, correctedMeasurement);
                        }
                        else
                            discardMeasurement = 1;
                    }

                    break;
                case 11:
                    try {
                        currentWeatherMeasurement[corrIndex] = Byte.parseByte(measurementEntry[i], 2);
                    }
                    catch (NumberFormatException e) { break; }

                    break;
                case 13:
                    try {
                        currentWeatherMeasurement[corrIndex] = Short.valueOf(measurementEntry[i]);
                        addToWeatherDataBuffers(corrIndex, currentWeatherMeasurement[corrIndex]);
                    }
                    catch (NumberFormatException e) {
                        float correctedMeasurement = WeatherMaths.calcLWMA(getWeatherDataBuffer(corrIndex), WeatherMaths.DataType.SHORT);

                        if (correctedMeasurement != Float.MIN_VALUE) {
                            currentWeatherMeasurement[corrIndex] = (short)correctedMeasurement;
                            addToWeatherDataBuffers(corrIndex, (short)correctedMeasurement);
                        }
                        else
                            discardMeasurement = 1;
                    }

                    break;
            }

            if(discardMeasurement == 1)
                break;
        }

        //System.out.println(Arrays.toString(currentWeatherMeasurement));
        if(discardMeasurement != 1)
            currentWeatherMeasurementsArr.add(currentWeatherMeasurement);
    }

    void clearWeatherMeasurements() {
        currentWeatherMeasurementsArr = new ArrayList<>();
    }


    // Weather data buffer arrays
    final static private byte WEATHER_DATA_ARRAY_SIZE = 30;

    private Object[][] weatherDataBufferArrs = new Object[10][WEATHER_DATA_ARRAY_SIZE];
    private byte[] weatherDataBufferPointers = new byte[10];

    private void addToWeatherDataBuffers(byte xmlIndex, Object data) {
        byte bufferIndex = getWeatherDataBufferIndex(xmlIndex);

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

    private byte getWeatherDataBufferIndex(byte xmlIndex) {
        // Get the buffer index
        /* XML to Buffer array conversion table: XML num - offset
        XML Line num    Buffer index num
        3 t/m 10  	    XML num - 2
            Skip 11, dus + 1 bij de offset
        12 en 13        XML num - 3
         */
        byte bufferIndex = (byte)(xmlIndex - 2);

        if(xmlIndex > 10)
            bufferIndex -= 1; // Subtract an extra value according to the XML to Buffer array conversion table

        return bufferIndex;
    }

    private Object[] getWeatherDataBuffer(byte xmlIndex) {
        byte bufferIndex = getWeatherDataBufferIndex(xmlIndex);
        return weatherDataBufferArrs[bufferIndex];
    }

    public void clearWeatherDataBuffers() {
        weatherDataBufferArrs = new Object[10][WEATHER_DATA_ARRAY_SIZE];
        weatherDataBufferPointers = new byte[10];
    }
}
