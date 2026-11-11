package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.movement.Velocity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntityPush
{
    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    private void xulu$noEntityPush(final double x, final double y, final double z, final CallbackInfo ci) {
        if ((Object)this == Minecraft.getInstance().player && Velocity.shouldCancelPush()) {
            ci.cancel();
        }
    }
}