package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.movement.GuiMove;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput
{
    @Inject(method = "tick", at = @At("TAIL"))
    private void xulu$guiMove(final boolean slowDown, final CallbackInfo info) {
        final GuiMove guiMove = Xulu.MODULE_MANAGER.getModuleT(GuiMove.class);
        if (guiMove == null || !guiMove.isToggled()) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen == null || mc.screen instanceof ChatScreen) {
            return;
        }
        final Input input = (Input)(Object)this;
        final long window = mc.getWindow().getWindow();
        input.up = InputConstants.isKeyDown(window, mc.options.keyUp.getKey().getValue());
        input.down = InputConstants.isKeyDown(window, mc.options.keyDown.getKey().getValue());
        input.left = InputConstants.isKeyDown(window, mc.options.keyLeft.getKey().getValue());
        input.right = InputConstants.isKeyDown(window, mc.options.keyRight.getKey().getValue());
        input.forwardImpulse = xulu$calculateImpulse(input.up, input.down);
        input.leftImpulse = xulu$calculateImpulse(input.left, input.right);
        input.jumping = InputConstants.isKeyDown(window, mc.options.keyJump.getKey().getValue());
        if (slowDown) {
            input.leftImpulse *= 0.3F;
            input.forwardImpulse *= 0.3F;
        }
    }

    private static float xulu$calculateImpulse(final boolean positive, final boolean negative) {
        if (positive == negative) {
            return 0.0F;
        }
        return positive ? 1.0F : -1.0F;
    }
}
