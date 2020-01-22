package org.bloomdex.weatherstation;

import org.bloomdex.weatherstation.connection.generator.GeneratorConnectionManager;
import org.bloomdex.weatherstation.counter.CounterManager;

public class Main {
    public static void main(String[] args) {
        CounterManager.startCounter(); // Start timer thread
        GeneratorConnectionManager.StartConnection(); // Start connection threads
    }
}
