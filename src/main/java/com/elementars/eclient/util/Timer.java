package com.elementars.eclient.util;

public class Timer
{
    private long lastMS;

    public Timer() {
        this.reset();
    }

    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

    public boolean hasReached(final double milliseconds) {
        return this.getCurrentMS() - this.lastMS >= milliseconds;
    }

    public boolean hasReached(final long milliseconds) {
        return this.getCurrentMS() - this.lastMS >= milliseconds;
    }

    public long getDifference() {
        return this.getCurrentMS() - this.lastMS;
    }
}
