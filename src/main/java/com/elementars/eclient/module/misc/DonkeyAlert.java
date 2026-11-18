package com.elementars.eclient.module.misc;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Donkey;

public class DonkeyAlert extends Module
{
    int delay;

    public DonkeyAlert() {
        super("DonkeyAlert", "Alerts you when a donkey is in your render distance", 0, Category.MISC, true);
    }

    @Override
    public void onEnable() {
        this.delay = 0;
    }

    @Override
    public void onUpdate() {
        if (this.delay > 0) {
            --this.delay;
        }
        if (DonkeyAlert.mc.level == null) {
            return;
        }
        for (final Entity entity : DonkeyAlert.mc.level.entitiesForRendering()) {
            if (entity instanceof Donkey && this.delay == 0) {
                Command.sendChatMessage("Donkey spotted!");
                this.delay = 200;
            }
        }
    }
}
