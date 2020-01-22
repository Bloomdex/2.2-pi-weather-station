package org.bloomdex.weatherstation.counter;

import org.bloomdex.weatherstation.connection.generator.GeneratorConnectionManager;

import java.util.Timer;

public class CounterManager {
    private static final int TIMER_SECONDS = 10;
    private static Timer timer;

    /**
     * Start a new thread with a counter.
     */
    public static void startCounter() {
        timer = new Timer();
        timer.schedule(new CounterThread(), TIMER_SECONDS * 1000);
    }

    /**
     * Stop the current counter thread, create a new one and call methods that rely on the counter.
     */
    static void resetCounter() {
        System.out.println("Timer: Done counting, restarting...");
        timer.cancel();
        startCounter();
        GeneratorConnectionManager.flipResponsibilityByte();
    }
}

