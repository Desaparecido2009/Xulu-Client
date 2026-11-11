package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.movement.Velocity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayer
{
    @Inject(method = "isPushedByFluid", at = @At("HEAD"), cancellable = true)
    private void xulu$isPushedByFluid(final CallbackInfoReturnable<Boolean> callback) {
        if ((Object)this == Minecraft.getInstance().player && Velocity.shouldCancelAll()) {
            callback.setReturnValue(false);
        }
    }
}
