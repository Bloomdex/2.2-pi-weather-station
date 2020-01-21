package org.bloomdex;

import org.bloomdex.weatherdata.WeatherMaths;

public class Main {
    public static void main(String[] args) {

        // Start connection threads
        ConnectionManager.StartConnection();

        //short[] vals = new short[] { 226, 226, 226, 226, 226, 226, 225, 226, 226, 226, 225, 226, 226, 226, 226, 226, 228, 228, 227, 227, 227, 226, 226, 226, 226, 226, 226, 226, 226, 226 };

        //System.out.println((WeatherMaths.calcLWMA((Float[])vals, WeatherMaths.DataType.SHORT));
    }
}
