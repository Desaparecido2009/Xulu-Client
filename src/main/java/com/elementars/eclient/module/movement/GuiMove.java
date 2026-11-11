package com.elementars.eclient.module.movement;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class GuiMove extends Module
{
    public GuiMove() {
        super("GuiMove", "Move in gui menus", 0, Category.MOVEMENT, true);
    }

    @Override
    public void onUpdate() {
        if (GuiMove.mc.player == null || GuiMove.mc.screen == null || GuiMove.mc.screen instanceof ChatScreen) {
            return;
        }
        final long window = GuiMove.mc.getWindow().getWindow();
        final KeyMapping[] keys = { GuiMove.mc.options.keyUp, GuiMove.mc.options.keyLeft, GuiMove.mc.options.keyRight, GuiMove.mc.options.keyDown, GuiMove.mc.options.keyJump };
        for (final KeyMapping key : keys) {
            key.setDown(InputConstants.isKeyDown(window, key.getKey().getValue()));
        }
        if (GuiMove.mc.screen instanceof AbstractContainerScreen) {
            if (InputConstants.isKeyDown(window, InputConstants.KEY_UP)) {
                GuiMove.mc.player.setXRot(GuiMove.mc.player.getXRot() - 7.0f);
            }
            if (InputConstants.isKeyDown(window, InputConstants.KEY_DOWN)) {
                GuiMove.mc.player.setXRot(GuiMove.mc.player.getXRot() + 7.0f);
            }
            if (InputConstants.isKeyDown(window, InputConstants.KEY_RIGHT)) {
                GuiMove.mc.player.setYRot(GuiMove.mc.player.getYRot() + 7.0f);
            }
            if (InputConstants.isKeyDown(window, InputConstants.KEY_LEFT)) {
                GuiMove.mc.player.setYRot(GuiMove.mc.player.getYRot() - 7.0f);
            }
            if (InputConstants.isKeyDown(window, GuiMove.mc.options.keySprint.getKey().getValue())) {
                GuiMove.mc.player.setSprinting(true);
            }
        }
    }
}
