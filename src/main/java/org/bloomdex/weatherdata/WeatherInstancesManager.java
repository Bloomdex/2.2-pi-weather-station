package org.bloomdex.weatherdata;

import java.util.HashMap;

public class WeatherInstancesManager {
    private static HashMap<String, WeatherStationInstance> weatherStationInstances = new HashMap<>();

    public static void updateInstances(String[] measurement) {
        getWeatherStationInstance(measurement[0]).addWeatherMeasurement(measurement);
    }

    private static WeatherStationInstance getWeatherStationInstance(String stn) {
        // Create a new instance if it isn't present yet
        if (weatherStationInstances.get(stn) == null)
            weatherStationInstances.put(stn, new WeatherStationInstance(stn));

        return weatherStationInstances.get(stn);
    }
}
