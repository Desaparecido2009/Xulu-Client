package com.elementars.eclient.util;

public class ConnectionUtil
{
    public static ConnectionUtil INSTANCE;
    private long lastPingTime;
    private int ping;

    public long getLastPingTime() {
        return this.lastPingTime;
    }

    public int getPing() {
        return this.ping;
    }

    public void setPing(final int ping) {
        this.lastPingTime = System.currentTimeMillis();
        this.ping = ping;
    }
}
