package org.bloomdex;

import org.bloomdex.client.ClientManager;
import org.bloomdex.helpers.ResourceHelper;
import org.bloomdex.weatherstation.generator.GeneratorConnectionManager;
import org.bloomdex.weatherstation.counter.CounterManager;

public class Main {
    public static void main(String[] args) {
        try {
            ResourceHelper.loadConfigProperties();
        }
        catch (NullPointerException e) { e.printStackTrace(); }

        if (args.length == 0 || !args[0].equals("NC"))
                ClientManager.CreateClient();

        CounterManager.startCounter(); // Start timer thread
        GeneratorConnectionManager.StartConnection(); // Start connection threads
    }
}
