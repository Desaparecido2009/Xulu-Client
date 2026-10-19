package com.elementars.eclient.font.rainbow;

public class RainbowCycle
{
    private float hue;

    public RainbowCycle() {
        this.hue = 0.0f;
    }

    public float getHue() {
        return this.hue;
    }

    public void update() {
        this.hue += 0.01f;
        if (this.hue > 1.0f) {
            this.hue = 0.0f;
        }
    }
}
