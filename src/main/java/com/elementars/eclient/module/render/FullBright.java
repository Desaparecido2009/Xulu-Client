package com.elementars.eclient.module.render;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;

public class FullBright extends Module
{
    double oldBrightness;

    public FullBright() {
        super("Fullbright", "Permanent brightness", 0, Category.RENDER, true);
    }

    @Override
    public void onEnable() {
        this.oldBrightness = FullBright.mc.options.gamma;
        FullBright.mc.options.gamma = 1000.0;
    }

    @Override
    public void onDisable() {
        FullBright.mc.options.gamma = this.oldBrightness;
    }

    @Override
    public void onUpdate() {
        if (FullBright.mc.options.gamma != 1000.0) {
            FullBright.mc.options.gamma = 1000.0;
        }
    }
}
