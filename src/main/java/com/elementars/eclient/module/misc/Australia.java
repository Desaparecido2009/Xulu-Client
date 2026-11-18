package com.elementars.eclient.module.misc;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class Australia extends Module
{
    private boolean shaderLoaded;

    public Australia() {
        super("Australia", "Best Module", 0, Category.MISC, true);
    }

    @Override
    public void onEnable() {
        this.shaderLoaded = false;
    }

    @Override
    public void onDisable() {
        if (Australia.mc.gameRenderer.currentEffect() != null) {
            Australia.mc.gameRenderer.shutdownEffect();
        }
        this.shaderLoaded = false;
    }

    @Override
    public void onUpdate() {
        if (Australia.mc.player == null || Australia.mc.level == null) {
            return;
        }
        if (Australia.mc.getCameraEntity() instanceof Player) {
            if (!this.shaderLoaded || Australia.mc.gameRenderer.currentEffect() == null) {
                try {
                    Australia.mc.gameRenderer.loadEffect(new ResourceLocation("shaders/post/flip.json"));
                    this.shaderLoaded = true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (this.shaderLoaded && Australia.mc.screen == null) {
            Australia.mc.gameRenderer.shutdownEffect();
            this.shaderLoaded = false;
        }
        Australia.mc.player.setSecondsOnFire(1);
    }
}
