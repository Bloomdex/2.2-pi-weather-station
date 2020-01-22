package org.bloomdex.weatherstation.counter;

import java.util.TimerTask;

class CounterThread extends TimerTask {
    /**
     * Tell the manager that the time limit has been reached.
     */
    public void run() {
        CounterManager.resetCounter();
    }
}
