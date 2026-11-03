package com.elementars.eclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;

public class HueCycler
{
    private int index;
    private final int[] cycles;

    public HueCycler(final int cycles) {
        this.index = 0;
        if (cycles <= 0) {
            throw new IllegalArgumentException("cycles <= 0");
        }
        this.cycles = new int[cycles];
        double hue = 0.0;
        final double add = 1.0 / cycles;
        for (int i = 0; i < cycles; ++i) {
            this.cycles[i] = Color.HSBtoRGB((float)hue, 1.0f, 1.0f);
            hue += add;
        }
    }

    public void reset() {
        this.index = 0;
    }

    public void reset(final int index) {
        this.index = index;
    }

    public int next() {
        final int color = this.cycles[this.index];
        ++this.index;
        if (this.index >= this.cycles.length) {
            this.index = 0;
        }
        return color;
    }

    public void setNext() {
        this.next();
    }

    public void set() {
        this.apply(this.cycles[this.index], 1.0f);
    }

    public void setNext(final float alpha) {
        this.apply(this.next(), alpha);
    }

    private void apply(final int rgb, final float alpha) {
        final float red = (rgb >> 16 & 0xFF) / 255.0f;
        final float green = (rgb >> 8 & 0xFF) / 255.0f;
        final float blue = (rgb & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public int current() {
        return this.cycles[this.index];
    }
}
