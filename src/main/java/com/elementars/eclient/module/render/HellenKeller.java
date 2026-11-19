package com.elementars.eclient.module.render;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.sounds.SoundSource;

public class HellenKeller extends Module
{
    private float masterLevel;
    private boolean captured;

    public HellenKeller() {
        super("HellenKeller", "Play like Hellen Keller", 0, Category.RENDER, true);
    }

    @Override
    public void onEnable() {
        this.masterLevel = HellenKeller.mc.options.getSoundSourceVolume(SoundSource.MASTER);
        this.captured = true;
        HellenKeller.mc.options.setSoundCategoryVolume(SoundSource.MASTER, 0.0f);
    }

    @Override
    public void onRender() {
        GuiComponent.fill(new PoseStack(), 0, 0, HellenKeller.mc.getWindow().getGuiScaledWidth(), HellenKeller.mc.getWindow().getGuiScaledHeight(), 0xFF000000);
    }

    @Override
    public void onDisable() {
        if (!this.captured) {
            return;
        }
        HellenKeller.mc.options.setSoundCategoryVolume(SoundSource.MASTER, this.masterLevel);
        this.captured = false;
    }
}
