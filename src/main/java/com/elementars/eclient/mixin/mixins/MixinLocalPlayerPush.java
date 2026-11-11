package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.movement.Velocity;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerPush
{
    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void xulu$noBlockPush(final double x, final double z, final CallbackInfo ci) {
        if (Velocity.shouldCancelPush()) {
            ci.cancel();
        }
    }
}