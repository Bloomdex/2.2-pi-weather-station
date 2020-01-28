package org.bloomdex.weatherstation.weatherdata;

import java.util.HashMap;

public class WeatherInstancesManager {
    private HashMap<String, WeatherStationInstance> weatherStationInstances = new HashMap<>();

    /**
     * Add a xmlSet to matching weather station instance based on station number if it exists.
     * @param xmlSet xmlSet that needs to be parsed by the matching weather station instance.
     */
    public void updateInstances(String[] xmlSet) {
        getWeatherStationInstance(xmlSet[0]).parseXMLSet(xmlSet);
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
}
