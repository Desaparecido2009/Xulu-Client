package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.render.NoHurtCam;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer
{
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void xulu$noHurtCam(final PoseStack poseStack, final float partialTicks, final CallbackInfo info) {
        if (NoHurtCam.shouldDisable()) {
            info.cancel();
        }
    }
}
