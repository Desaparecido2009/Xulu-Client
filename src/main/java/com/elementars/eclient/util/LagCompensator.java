package com.elementars.eclient.util;

public class LagCompensator
{
    public static LagCompensator INSTANCE;
    private float time;

    public LagCompensator() {
        this.time = 50.0f;
    }

    public float getTime() {
        return this.time;
    }
}
