package com.elementars.eclient.module.player;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.client.KeyMapping;

public class AutoWalk extends Module
{
    public AutoWalk() {
        super("AutoWalk", "Automatically walks", 0, Category.PLAYER, true);
    }

    @Override
    public void onUpdate() {
        KeyMapping.set(AutoWalk.mc.options.keyUp.getKey(), true);
    }

    @Override
    public void onDisable() {
        KeyMapping.set(AutoWalk.mc.options.keyUp.getKey(), false);
    }
}
