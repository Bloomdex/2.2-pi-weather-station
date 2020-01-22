package org.bloomdex.weatherstation.weatherdata;

import java.util.HashMap;

public class WeatherInstancesManager {
    private HashMap<String, WeatherStationInstance> weatherStationInstances = new HashMap<>();

    /**
     * Add a measurement to matching weather station instance based on station number if it exists.
     * @param measurement measurement that needs to be added to the matching weather station instance.
     */
    public void updateInstances(String[] measurement) {
        getWeatherStationInstance(measurement[0]).addWeatherMeasurement(measurement);
    }

    /**
     * Provide an existing weather station instance, if it doesn't exist it'll be created.
     * @param stn station number that should be found or created.
     * @return the matching weather station instance.
     */
    private WeatherStationInstance getWeatherStationInstance(String stn) {
        // Create a new instance if it isn't present yet
        if (weatherStationInstances.get(stn) == null)
            weatherStationInstances.put(stn, new WeatherStationInstance());

        return weatherStationInstances.get(stn);
    }

    /**
     * Handle all the data in every weather station instance that exists.
     */
    public void handleData() {
        for (WeatherStationInstance weatherStationInstance: weatherStationInstances.values()) {
            weatherStationInstance.clearWeatherMeasurements();
            //weatherStationInstance.clearBuffers();
        }
    }
}
