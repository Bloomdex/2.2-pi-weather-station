package org.bloomdex.weatherdata;

import java.util.HashMap;

public class WeatherInstancesManager {
    private HashMap<String, WeatherStationInstance> weatherStationInstances = new HashMap<>();

    public void updateInstances(String[] measurement) {
        getWeatherStationInstance(measurement[0]).addWeatherMeasurement(measurement);
    }

    private WeatherStationInstance getWeatherStationInstance(String stn) {
        // Create a new instance if it isn't present yet
        if (weatherStationInstances.get(stn) == null)
            weatherStationInstances.put(stn, new WeatherStationInstance(stn));

        return weatherStationInstances.get(stn);
    }

    public void handleData() {
        // Only clears measurements and buffers for now
        for (WeatherStationInstance weatherStationInstance: weatherStationInstances.values()) {
            weatherStationInstance.clearWeatherMeasurements();
            //weatherStationInstance.clearBuffers();
        }
    }
}
