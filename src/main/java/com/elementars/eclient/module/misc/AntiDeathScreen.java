package com.elementars.eclient.module.misc;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.client.gui.screens.DeathScreen;

public class AntiDeathScreen extends Module
{
    public AntiDeathScreen() {
        super("AntiDeathScreen", "AntiDeathScreen apparently", 0, Category.MISC, true);
    }

    @Override
    public void onUpdate() {
        if (this.isToggled() && AntiDeathScreen.mc.screen instanceof DeathScreen && AntiDeathScreen.mc.player != null && AntiDeathScreen.mc.player.getHealth() > 0.0f) {
            AntiDeathScreen.mc.player.respawn();
            AntiDeathScreen.mc.setScreen(null);
        }
    }
}
