package org.bloomdex;

import java.util.Timer;
import java.util.TimerTask;

public class CounterManager {
    Timer timer;
    private int timerSeconds;

    public CounterManager(int seconds) {
        timerSeconds = seconds;

        timer = new Timer();
        timer.schedule(new RemindTask(), timerSeconds * 1000);
    }

    public void reset() {
        timer.cancel();

        timer = new Timer();
        timer.schedule(new RemindTask(), timerSeconds * 1000);
    }

    class RemindTask extends TimerTask {
        public void run() {
            System.out.println("Timer: Done counting, restarting...");
            ConnectionManager.flipResponsibilityByte();
            reset();
        }
    }
}