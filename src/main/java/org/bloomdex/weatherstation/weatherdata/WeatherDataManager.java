package org.bloomdex.weatherstation.weatherdata;

import java.util.ArrayList;
import java.util.Arrays;

public class WeatherDataManager {
    private static ArrayList<Byte> parsedMeasurementSets = new ArrayList<>();
    private static int measurementSetAmount = 0;
    private static long measurementSetMaxAmount = 0;

    /**
     * Extends the list holding all parsed measurements with a new parsedMeasurementSet.
     * @param parsedMeasurementSet the parsed measurement set that needs to be added to the list.
     */
    static void storeParsedMeasurementSet(Byte[] parsedMeasurementSet) {
        synchronized (parsedMeasurementSets) {
            measurementSetAmount += 1;
            measurementSetMaxAmount += 1;
            parsedMeasurementSets.addAll(Arrays.asList(parsedMeasurementSet));
        }
    }

    /**
     * @return the list of parsed measurements consisting of bytes.
     */
    public static ArrayList<Byte> getParsedMeasurementSets() { return parsedMeasurementSets; }

    /**
     * @return the list of parsed measurements consisting of bytes as a primary data type array.
     */
    public static byte[] getParsedMeasurementSetsPrim() {
        byte[] parsedMeasurementSetsPrim;

        synchronized (parsedMeasurementSets) {
            parsedMeasurementSetsPrim = new byte[parsedMeasurementSets.size()];

            for(int i = 0; i < parsedMeasurementSetsPrim.length; i++) {
                parsedMeasurementSetsPrim[i] = parsedMeasurementSets.get(i);
            }
        }

        return parsedMeasurementSetsPrim;
    }

    /**
     * @return the amount of measurements.
     */
    public static int getMeasurementSetAmount() { return measurementSetAmount; }

    /**
     * @return the maximum amount of measurements.
     */
    public static long getMeasurementSetMaxAmount() { return measurementSetMaxAmount; }

    /**
     * Reset all data gathered and value holding amount of measurements.
     */
    public static void resetData() {
        parsedMeasurementSets.clear();
        measurementSetAmount = 0;
    }
}
