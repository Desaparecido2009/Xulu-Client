package com.elementars.eclient.util;

public class Location
{
    private double x;
    private double y;
    private double z;

    public Location(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }
}
