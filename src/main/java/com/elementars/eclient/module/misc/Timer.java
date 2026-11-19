package com.elementars.eclient.module.misc;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;

public class Timer extends Module
{
    private static float speed = 1.0f;
    private final Value<Float> tickSpeed;

    public Timer() {
        super("Timer", "Modifies the game speed", 0, Category.MISC, true);
        this.tickSpeed = this.register(new Value<Float>("Speed", this, 4.0f, 0.0f, 10.0f));
    }

    public static float getSpeed() {
        return Timer.speed;
    }

    @Override
    public void onDisable() {
        Timer.speed = 1.0f;
    }

    @Override
    public void onUpdate() {
        Timer.speed = (this.tickSpeed.getValue() == 0.0f) ? 0.1f : this.tickSpeed.getValue();
    }
}
