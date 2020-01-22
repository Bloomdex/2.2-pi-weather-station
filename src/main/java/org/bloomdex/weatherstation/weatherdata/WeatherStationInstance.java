package org.bloomdex.weatherstation.weatherdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

class WeatherStationInstance {
    // region Measurement operations

    private SimpleDateFormat simpleDateFormat;
    private ArrayList<Object[]> currentWeatherMeasurementsArr = new ArrayList<>();

    WeatherStationInstance() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Add a mearuement entry to the instance after data has been checked and corrected if needed.
     * @param measurementEntry the measurement entry that needs to be added to the instance.
     */
    void addWeatherMeasurement(String[] measurementEntry) {
        Object[] currentWeatherMeasurement = new Object[14];

        String dateString = ""; // Data that is being constructed in the for loop
        byte corrOffset = 0; // Offset used by the corrIndex based on position in the measurementEntry array
        byte corrIndex; // Used when doing buffer operations or when adding to the currentWeatherMeasurement object
        boolean discardMeasurement = false; // Boolean that tells whether this measurement should be discarded or not

        for(byte i = 0; i < measurementEntry.length; i++) {
            // Calculate the corrIndex based on measurementEntry array position
            corrIndex = (byte)(i - corrOffset);

            if (i == 0) {
                currentWeatherMeasurement[corrIndex] = Integer.parseInt(measurementEntry[i]);
            }
            else if (i == 1 || i == 2) {
                if(i == 1) {
                    dateString = measurementEntry[i];
                }
                else {
                    try{
                        Date parsedDateTime = simpleDateFormat.parse(dateString + " " + measurementEntry[i]);
                        currentWeatherMeasurement[corrIndex - 1] = (int)(parsedDateTime.getTime() / 1000);
                    }
                    catch(ParseException e) {
                        // Operation failed because date or time had an error, discard the measurement
                        discardMeasurement = true;
                    }

                    corrOffset = 1;
                }
            }
            else if ((i >= 3 && i <= 10) || i == 12) {
                float expectedMeasurement = WeatherMaths.calcLWMA(
                        getWeatherDataBuffer(corrIndex),
                        WeatherMaths.DataType.FLOAT);

                try {
                    float currentMeasurement = Float.parseFloat(measurementEntry[i]);

                    // Check if the measurementEntry array position is at the temperature measurement
                    if (i == 3) {
                        // Temperature measurement should be handled with extra correction and checks
                        if (expectedMeasurement != Float.MIN_VALUE) {
                            float differPercentage = (expectedMeasurement - currentMeasurement) / currentMeasurement;

                            // Check if the difference in percentage between the current and expected measurement
                            // Is bigger or smaller than 20%
                            if (differPercentage <= -0.2 || differPercentage >= 0.2)
                                currentMeasurement = expectedMeasurement;
                        }
                    }

                    currentWeatherMeasurement[corrIndex] = currentMeasurement;
                    addToWeatherDataBuffers(corrIndex, currentWeatherMeasurement[corrIndex]);
                }
                catch(NumberFormatException e) {
                    // Given measurement could not be converted to a float,
                    // calculate an expected measurement based on the past measurements present in the buffer
                    if (expectedMeasurement != Float.MIN_VALUE) {
                        currentWeatherMeasurement[corrIndex] = expectedMeasurement;
                        addToWeatherDataBuffers(corrIndex, expectedMeasurement);
                    }
                    else // Given measurement was incorrect and could not be corrected, discard the measurement
                        discardMeasurement = true;
                }
            }
            else if (i == 11) {
                try {
                    currentWeatherMeasurement[corrIndex] = Byte.parseByte(measurementEntry[i], 2);
                }
                catch (NumberFormatException e) {
                    // Given measurement was missing and cannot be corrected using extrapolation, discard the measurement
                    discardMeasurement = true;
                }
            }
            else if (i == 13) {
                try {
                    currentWeatherMeasurement[corrIndex] = Short.valueOf(measurementEntry[i]);
                    addToWeatherDataBuffers(corrIndex, currentWeatherMeasurement[corrIndex]);
                }
                catch (NumberFormatException e) {
                    // Given measurement could not be converted to a short,
                    // calculate an expected measurement based on the past measurements present in the buffer
                    float expectedMeasurement = WeatherMaths.calcLWMA(
                            getWeatherDataBuffer(corrIndex),
                            WeatherMaths.DataType.SHORT);

                    if (expectedMeasurement != Float.MIN_VALUE) {
                        currentWeatherMeasurement[corrIndex] = (short)expectedMeasurement;
                        addToWeatherDataBuffers(corrIndex, (short)expectedMeasurement);
                    }
                    else // Given measurement was incorrect and could not be corrected, discard the measurement
                        discardMeasurement = true;
                }
            }

            if(discardMeasurement)
                break;
        }

        //System.out.println(Arrays.toString(currentWeatherMeasurement));
        if(!discardMeasurement) {
            currentWeatherMeasurementsArr.add(currentWeatherMeasurement);
            System.out.println(Arrays.toString(currentWeatherMeasurement));
        }
    }

    /**
     * Clear all weather measurement objects in the weather measurements array
     */
    void clearWeatherMeasurements() {
        currentWeatherMeasurementsArr = new ArrayList<>();
    }
    // endregion


    // region Buffer operations

    // Weather data buffer arrays
    final static private byte WEATHER_DATA_ARRAY_SIZE = 30;

    private Object[][] weatherDataBufferArrs = new Object[10][WEATHER_DATA_ARRAY_SIZE];
    private byte[] weatherDataBufferPointers = new byte[10];

    /**
     * Adds a measurement to a buffer that corresponds to the given xml index.
     * @param xmlIndex the index of the current position in the xml file.
     * @param data the data that should be added to the buffer.
     */
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

    /**
     * Convert the xmlIndex to a buffer index since some values of the xml don't have to be added to the buffer.
     * @param xmlIndex the index of the current position in the xml file.
     * @return the index that can be used to retrieve a buffer.
     */
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

    /**
     * Get a buffer using a given xml index.
     * @param xmlIndex the index of the current position in the xml file.
     * @return the buffer that can be used to add a measurement to.
     */
    private Object[] getWeatherDataBuffer(byte xmlIndex) {
        byte bufferIndex = getWeatherDataBufferIndex(xmlIndex);
        return weatherDataBufferArrs[bufferIndex];
    }
    // endregion
}
