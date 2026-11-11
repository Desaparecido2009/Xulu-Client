package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.events.MotionEvent;
import com.elementars.eclient.event.events.MotionEventPost;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer
{
    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayer(final CallbackInfo info) {
        final MotionEvent event = new MotionEvent();
        event.call();
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "sendPosition", at = @At("RETURN"))
    private void onUpdateWalkingPlayerPost(final CallbackInfo info) {
        final MotionEventPost event = new MotionEventPost();
        event.call();
    }

    @ModifyVariable(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), argsOnly = true)
    private Vec3 xulu$onMovePre(final Vec3 movement, final MoverType type) {
        final PlayerMoveEvent event = new PlayerMoveEvent(type, movement.x, movement.y, movement.z);
        event.setState(Event.State.PRE);
        event.call();
        if (event.isCancelled() || event.getX() != movement.x || event.getY() != movement.y || event.getZ() != movement.z) {
            return new Vec3(event.getX(), event.getY(), event.getZ());
        }
        return movement;
    }

    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("RETURN"))
    private void xulu$onMovePost(final MoverType type, final Vec3 movement, final CallbackInfo info) {
        final PlayerMoveEvent event = new PlayerMoveEvent(type, movement.x, movement.y, movement.z);
        event.setState(Event.State.POST);
        event.call();
    }
}
