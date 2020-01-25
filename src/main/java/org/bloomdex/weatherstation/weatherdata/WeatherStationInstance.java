package org.bloomdex.weatherstation.weatherdata;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class WeatherStationInstance {
    // region Measurement operations

    private SimpleDateFormat simpleDateFormat;
    private ArrayList<byte[]> parsedMeasurementsArr = new ArrayList<>();

    WeatherStationInstance() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Add a measurement set to the instance after given XML has been parsed, checked and corrected if needed.
     * @param xmlSet the XML set that needs to be parsed by and added to the instance.
     */
    void parseXMLSet(String[] xmlSet) {
        byte[] convertedBytesArr = new byte[47];
        byte convertedByteArrIndex = 0;
        byte corrIndex; // Used when doing buffer operations
        byte xmlLinesSkipped = 0; // Offset used by the corrIndex based on position in the XML set
        boolean discardMeasurement = false; // Boolean that tells whether this measurement should be discarded or not

        for (byte i = 0; i < xmlSet.length; i++) {
            Object measurementToBeBuffered = null;
            byte[] convertedMeasurementToBeSaved = null;
            corrIndex = (byte)(i - xmlLinesSkipped); // Calculate the corrIndex based on xmlSet array position

            if (i == 0)
                convertedMeasurementToBeSaved = ByteBuffer.allocate(4).putInt(Integer.parseInt(xmlSet[i])).array();
            else if (i == 2) {
                try{
                    Date dateTime = simpleDateFormat.parse(xmlSet[i-1] + " " + xmlSet[i]);
                    int parsedDateTime = (int)(dateTime.getTime() / 1000);

                    convertedMeasurementToBeSaved = ByteBuffer.allocate(Integer.BYTES).putInt(parsedDateTime).array();
                    xmlLinesSkipped = 1; // We skipped the second line with index 1
                }
                catch(ParseException e) { discardMeasurement = true; }
            }
            else if (i >= 3 && i <= 10 || i == 12) {
                // calculate an expected measurement based on the past measurements present in the buffer
                float expectedMeasurement = WeatherMaths.calcLWMA(
                        getMeasurementBuffer(corrIndex),
                        WeatherMaths.DataType.FLOAT);

                if (xmlSet[i].length() != 0) {
                    float currentMeasurement = Float.parseFloat(xmlSet[i]);

                    // Check if the xmlSet array position is at the temperature measurement
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

                    convertedMeasurementToBeSaved = ByteBuffer.allocate(Float.BYTES).putFloat(currentMeasurement).array();
                    measurementToBeBuffered = currentMeasurement;
                }
                else {
                    if (expectedMeasurement != Float.MIN_VALUE) {
                        convertedMeasurementToBeSaved = ByteBuffer.allocate(Float.BYTES).putFloat(expectedMeasurement).array();
                        measurementToBeBuffered = expectedMeasurement;
                    }
                    else // Given measurement was incorrect and could not be corrected, discard the measurement
                        discardMeasurement = true;
                }
            }
            else if (i == 11) {
                if (xmlSet[i].length() != 0)
                    convertedMeasurementToBeSaved = new byte[] { Byte.parseByte(xmlSet[i], 2) };
                else
                    discardMeasurement = true;
            }
            else if (i == 13) {
                short measurementToUse = 0;

                if (xmlSet[i].length() != 0)
                    measurementToUse = Short.parseShort(xmlSet[i]);
                else {
                    // calculate an expected measurement based on the past measurements present in the buffer
                    float expectedMeasurement = WeatherMaths.calcLWMA(
                            getMeasurementBuffer(corrIndex),
                            WeatherMaths.DataType.SHORT);

                    if (expectedMeasurement != Float.MIN_VALUE)
                        measurementToUse = (short)expectedMeasurement;
                    else
                        discardMeasurement = true;
                }

                convertedMeasurementToBeSaved = ByteBuffer.allocate(Short.BYTES).putShort(measurementToUse).array();
                measurementToBeBuffered = measurementToUse;
            }

            // Handle given measurement to be buffered
            if (measurementToBeBuffered != null)
                addToBuffers(corrIndex, measurementToBeBuffered);

            // Handle given converted measurement to be saved
            if (convertedMeasurementToBeSaved != null) {
                for (byte measurementByte : convertedMeasurementToBeSaved) {
                    convertedBytesArr[convertedByteArrIndex] = measurementByte;
                    convertedByteArrIndex += 1;
                }
            }

            if(discardMeasurement)
                break;
        }

        if(!discardMeasurement)
            parsedMeasurementsArr.add(convertedBytesArr);
    }

    /**
     * Clear all parsed measurements in the parsedMeasurementsArr
     */
    void clearParsedMeasurements() {
        parsedMeasurementsArr = new ArrayList<>();
    }
    // endregion


    // region Buffer operations

    // Weather data buffer arrays
    final static private byte MEASUREMENT_BUFFER_ARR_SIZE = 30;

    private Object[][] measurementBufferArrs = new Object[10][MEASUREMENT_BUFFER_ARR_SIZE];
    private byte[] measurementBufferPointerArr = new byte[10];

    /**
     * Adds a measurement to a buffer that corresponds to the given XML set index.
     * @param xmlSetIndex the index of the current position in the xml file.
     * @param measurement the measurement that should be added to the buffer.
     */
    private void addToBuffers(byte xmlSetIndex, Object measurement) {
        byte bufferIndex = getMeasurementBufferIndex(xmlSetIndex);

        // Update the current buffer
        byte currentWeatherDataBufferPointer = measurementBufferPointerArr[bufferIndex];
        measurementBufferArrs[bufferIndex][currentWeatherDataBufferPointer] = measurement;
        /*System.out.println("Buffer: " + Arrays.toString(weatherDataBufferArrs[bufferIndex])
                + "\n\tWith buffer index: " + bufferIndex
                +"\n\tWith pointer: " + currentWeatherDataBufferPointer
                + "\n\tWith measurement: " + measurement);*/

        // Set the new buffer pointer
        currentWeatherDataBufferPointer += 1;

        if (currentWeatherDataBufferPointer == MEASUREMENT_BUFFER_ARR_SIZE)
            measurementBufferPointerArr[bufferIndex] = 0;
        else
            measurementBufferPointerArr[bufferIndex] = currentWeatherDataBufferPointer;
    }

    /**
     * Convert the xmlSetIndex to a buffer index since some values of the xml don't have to be added to the buffer.
     * @param xmlSetIndex the index of the current position in the xml file.
     * @return the index that can be used to retrieve a buffer.
     */
    private byte getMeasurementBufferIndex(byte xmlSetIndex) {
        // Get the buffer index
        /* XML to Buffer array conversion table: XML num - offset
        XML Line num    Buffer index num
        3 t/m 10  	    XML num - 2
            Skip 11, dus + 1 bij de offset
        12 en 13        XML num - 3
         */
        byte bufferIndex = (byte)(xmlSetIndex - 2);

        if(xmlSetIndex > 10)
            bufferIndex -= 1; // Subtract an extra value according to the XML to Buffer array conversion table

        return bufferIndex;
    }

    /**
     * Get a buffer using a given XML set index.
     * @param xmlSetIndex the index of the current position in the XML file.
     * @return the buffer that can be used to add a measurement to.
     */
    private Object[] getMeasurementBuffer(byte xmlSetIndex) {
        byte bufferIndex = getMeasurementBufferIndex(xmlSetIndex);
        return measurementBufferArrs[bufferIndex];
    }
    // endregion
}
