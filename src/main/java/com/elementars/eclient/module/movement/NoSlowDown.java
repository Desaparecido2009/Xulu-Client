package com.elementars.eclient.module.movement;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoSlowDown extends Module
{
    public NoSlowDown() {
        super("NoSlowDown", "Prevents slowdown when using items", 0, Category.MOVEMENT, true);
    }

    @SubscribeEvent
    public void onInput(final MovementInputUpdateEvent event) {
        if (NoSlowDown.mc.player == null) {
            return;
        }
        if (NoSlowDown.mc.player.isUsingItem() && !NoSlowDown.mc.player.isPassenger()) {
            event.getInput().leftImpulse *= 5.0f;
            event.getInput().forwardImpulse *= 5.0f;
        }
    }

    @Override
    public void onEnable() {
        NoSlowDown.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        NoSlowDown.EVENT_BUS.unregister(this);
    }
}
