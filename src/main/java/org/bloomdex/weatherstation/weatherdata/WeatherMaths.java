package org.bloomdex.weatherstation.weatherdata;

class WeatherMaths {
    static float calcEMA(Float lastMeasurement, Float lastEma) {
        float weight = 0.06542f;

        if (lastMeasurement == null && lastEma == null)
            return Float.MIN_VALUE;
        else if(lastMeasurement != null && lastEma == null)
            return lastMeasurement;

        return weight * lastMeasurement + (1 - weight) * lastEma;
    }

    static short calcEMA(Short lastMeasurement, Short lastEma) {
        float weight = 0.06542f;

        if (lastMeasurement == null && lastEma == null)
            return Short.MIN_VALUE;
        else if(lastMeasurement != null && lastEma == null)
            return Short.MAX_VALUE;

        return (short)(weight * lastMeasurement + (1 - weight) * lastEma);
    }
}
