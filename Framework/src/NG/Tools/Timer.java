package NG.Tools;

import NG.DataStructures.Tracked.TrackedInteger;

/**
 * @author Jorren Hendriks & Geert van Ieperen adapter design pattern
 */
public class Timer {

    private final TrackedInteger time;
    private final long startTime;

    public Timer() {
        startTime = System.currentTimeMillis();
        time = new TrackedInteger(0);
    }

    /**
     * @return The current system time.
     */
    public long getSystemTime() {
        return System.currentTimeMillis();
    }

    /**
     * @return the time at the call of updateLooptime
     */
    public long getTime() {
        return time.current();
    }

    /**
     * @return The number of milliseconds between the previous two gameticks.
     */
    public long getElapsedTime() {
        return time.current() - time.previous();
    }

    /**
     * @return The elapsed time in seconds between the previous two gameticks.
     */
    public float getElapsedSeconds() {
        return (getElapsedTime() / 1000f);
    }

    /**
     * @return the number of miliseconds since the last update in the loop
     */
    public long getTimeSinceLastUpdate() {
        return actualCurrent() - time.current();
    }

    /**
     * set loopTimer to current system time. Should only be called by NG.Engine, exactly once per loop step
     */
    public void updateLoopTime() {
        time.update(actualCurrent());
    }

    private int actualCurrent() {
        return (int) (System.currentTimeMillis() - startTime);
    }

}
