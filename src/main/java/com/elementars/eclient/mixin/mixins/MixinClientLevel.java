package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.player.Speedmine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel
{
    @Inject(method = "destroyBlockProgress", at = @At("HEAD"), cancellable = true)
    private void xulu$destroyBlockProgress(final int breakerId, final BlockPos pos, final int progress, final CallbackInfo info) {
        final Speedmine speedmine = Speedmine.getInstance();
        if (speedmine == null || !speedmine.isToggled() || !speedmine.noBreakAnim.getValue()) {
            return;
        }
        if (progress >= 0 && Minecraft.getInstance().player != null && breakerId == Minecraft.getInstance().player.getId()) {
            info.cancel();
        }
    }
}
