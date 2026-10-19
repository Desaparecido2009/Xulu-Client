package com.elementars.eclient.util;

import java.awt.Color;

public class HueCycler
{
    private int index;
    private int[] cycles;

    public HueCycler(final int cycles) {
        this.cycles = new int[cycles];
    }

    public void set(final float hue) {
    }

    public void reset() {
        this.index = 0;
    }

    public int next() {
        return this.current();
    }

    public int current() {
        return Color.HSBtoRGB(this.index / 360.0f, 1.0f, 1.0f);
    }
}
