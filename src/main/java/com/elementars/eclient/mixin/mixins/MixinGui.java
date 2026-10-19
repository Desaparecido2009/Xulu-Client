package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui
{
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void xulu$hidePotions(final PoseStack poseStack, final CallbackInfo info) {
        if ((Boolean)Xulu.VALUE_MANAGER.getValueByName("Hide Potions").getValue()) {
            info.cancel();
        }
    }
}
