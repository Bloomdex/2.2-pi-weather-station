package org.bloomdex.weatherstation.weatherdata;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class WeatherStationInstance {
    // region Measurement operations

    private SimpleDateFormat simpleDateFormat;

    WeatherStationInstance() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Add a measurement set to the instance after given XML has been parsed, checked and corrected if needed.
     * @param xmlSet the XML set that needs to be parsed by and added to the instance.
     */
    void parseXMLSet(String[] xmlSet) {
        Byte[] convertedBytesArr = new Byte[47];
        byte convertedByteArrIndex = 0;
        byte corrIndex; // Used when doing buffer operations
        byte xmlLinesSkipped = 0; // Offset used by the corrIndex based on position in the XML set
        boolean discardMeasurement = false; // Boolean that tells whether this measurement should be discarded or not

        // Parse the given xml set
        for (byte i = 0; i < xmlSet.length; i++) {
            Object measurementToBeSaved = null;
            Object emaToBeSaved = null;
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
                float expectedMeasurement = WeatherMaths.calcEMA((Float)getLastMeasurement(corrIndex),
                        (Float)getLastEma(corrIndex));

                // Set measurementToBeSaved
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

                    measurementToBeSaved = currentMeasurement;
                    convertedMeasurementToBeSaved = ByteBuffer.allocate(Float.BYTES).putFloat(currentMeasurement).array();
                }
                else if (expectedMeasurement != Float.MIN_VALUE) {
                    measurementToBeSaved = expectedMeasurement;
                    convertedMeasurementToBeSaved = ByteBuffer.allocate(Float.BYTES).putFloat(expectedMeasurement).array();
                }
                else
                    discardMeasurement = true;

                // Set emaToBeSaved
                if (expectedMeasurement != Float.MIN_VALUE)
                    emaToBeSaved = expectedMeasurement;
            }
            else if (i == 11) {
                if (xmlSet[i].length() != 0)
                    convertedMeasurementToBeSaved = new byte[] { Byte.parseByte(xmlSet[i], 2) };
                else
                    discardMeasurement = true;
            }
            else if (i == 13) {
                short expectedMeasurement = WeatherMaths.calcEMA((Short)getLastMeasurement(corrIndex),
                        (Short)getLastEma(corrIndex));

                // Set measurementToBeSaved
                if (xmlSet[i].length() != 0) {
                    measurementToBeSaved = Short.parseShort(xmlSet[i]);
                    convertedMeasurementToBeSaved = ByteBuffer.allocate(Short.BYTES).putShort((short)measurementToBeSaved).array();
                }
                else if (expectedMeasurement != Float.MIN_VALUE) {
                    measurementToBeSaved = expectedMeasurement;
                    convertedMeasurementToBeSaved = ByteBuffer.allocate(Short.BYTES).putShort((short)measurementToBeSaved).array();
                }
                else
                    discardMeasurement = true;

                // Set emaToBeSaved
                if (expectedMeasurement != Short.MIN_VALUE)
                    emaToBeSaved = expectedMeasurement;
            }

            // Finish parsing this measurement by saving the values or discarding them
            if(!discardMeasurement) {
                // Handle given measurement and ema to be saved
                if (measurementToBeSaved != null)
                    setLastMeasurement(corrIndex, measurementToBeSaved);

                if (emaToBeSaved != null)
                    setLastEma(corrIndex, emaToBeSaved);

                // Handle given converted measurement to be saved
                if (convertedMeasurementToBeSaved != null) {
                    for (byte measurementByte : convertedMeasurementToBeSaved) {
                        convertedBytesArr[convertedByteArrIndex] = measurementByte;
                        convertedByteArrIndex += 1;
                    }
                }
            }
            else // Given measurement was incorrect and could not be corrected, discard the measurement
                break;
        }

        // Add the parsed data to the total collection
        if(!discardMeasurement)
            WeatherDataManager.storeParsedMeasurementSet(convertedBytesArr);
    }
    // endregion


    // region Calculation operations
    private Object[] lastMeasurements = new Object[10];
    private Object[] lastEmas = new Object[10];

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
        if (xmlSetIndex <= 10)
            return (byte)(xmlSetIndex - 2);
        else
            return (byte)(xmlSetIndex - 3);
    }

    /**
     * @param xmlSetIndex the index of the current position in the xml file.
     * @return the last calculated measurement
     */
    private Object getLastMeasurement(byte xmlSetIndex) {
        return lastMeasurements[getMeasurementBufferIndex(xmlSetIndex)];
    }

    /**
     * @param xmlSetIndex the index of the current position in the xml file.
     * @param value value to be set for the corresponding buffer.
     */
    private void setLastMeasurement(byte xmlSetIndex, Object value) {
        lastMeasurements[getMeasurementBufferIndex(xmlSetIndex)] = value;
    }

    /**
     * @param xmlSetIndex the index of the current position in the xml file.
     * @return the last calculated ema.
     */
    private Object getLastEma(byte xmlSetIndex) {
        return lastEmas[getMeasurementBufferIndex(xmlSetIndex)];
    }

    /**
     * @param xmlSetIndex the index of the current position in the xml file.
     * @param value value to be set for the corresponding buffer.
     */
    private void setLastEma(byte xmlSetIndex, Object value) {
        lastEmas[getMeasurementBufferIndex(xmlSetIndex)] = value;
    }
    // endregion
}
