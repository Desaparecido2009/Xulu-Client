package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.EntityEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Entity.class, priority = 9999)
public class MixinEntity
{
    @Redirect(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    public void xulu$onEntityCollision(final Entity entity, final double x, final double y, final double z) {
        final EntityEvent.EntityCollision event = new EntityEvent.EntityCollision(entity, x, y, z);
        event.call();
        if (event.isCancelled()) {
            return;
        }
        entity.push(event.getX(), event.getY(), event.getZ());
    }
}
