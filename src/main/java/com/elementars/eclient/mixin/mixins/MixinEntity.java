package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.EntityEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 9999)
public class MixinEntity
{
    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    private void xulu$onPush(final double x, final double y, final double z, final CallbackInfo callback) {
        final Entity entity = (Entity)(Object)this;
        final EntityEvent.EntityCollision event = new EntityEvent.EntityCollision(entity, x, y, z);
        event.call();
        if (event.isCancelled()) {
            callback.cancel();
            return;
        }
        if (event.getX() != x || event.getY() != y || event.getZ() != z) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(event.getX(), event.getY(), event.getZ()));
            entity.hasImpulse = true;
            callback.cancel();
        }
    }
}
