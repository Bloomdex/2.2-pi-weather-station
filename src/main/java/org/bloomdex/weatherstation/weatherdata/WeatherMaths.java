package org.bloomdex.weatherstation.weatherdata;

class WeatherMaths {
    public enum DataType { FLOAT, SHORT }

    /**
     * Calculate the linearly weighted moving average.
     * @param measurements data set used by the calculation.
     * @param dataType data type that the calculation should use.
     * @return the calculated linearly weighted moving average using the measurements.
     */
    static float calcLWMA(Object[] measurements, DataType dataType) {
        float sumWeightCoefficients = 0;
        float totalWeight = 0;

        for (byte i = 0; i < measurements.length; i++) {
            if (measurements[i] == null)
                break;

            if (dataType == DataType.FLOAT)
                sumWeightCoefficients += (i + 1) * (float)measurements[i];
            else if (dataType == DataType.SHORT)
                sumWeightCoefficients += (i + 1) * (short)measurements[i];

            totalWeight += (i + 1);
        }

        if (totalWeight != 465)
            return Float.MIN_VALUE; // Returns the minimum value of a float to avoid having to use Float objects

        return sumWeightCoefficients / totalWeight;
    }

    /*static float calcEWMA(float lastSMA, float currentMeasurement) {
        // EMA = [Close - previous EMA] * (2 / n+1) + previous EMA
        float EMA = (currentMeasurement - closeMeasurement) * (2f / 31f) + closeMeasurement;
    }*/
}
