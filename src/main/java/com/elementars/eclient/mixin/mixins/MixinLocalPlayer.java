package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.events.MotionEvent;
import com.elementars.eclient.event.events.MotionEventPost;
import com.elementars.eclient.event.events.PlayerMoveEvent;
import com.elementars.eclient.module.ModuleManager;
import com.elementars.eclient.module.render.OffhandSwing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "handleNetherPortalClient", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void xulu$portalChatSetScreen(final Minecraft minecraft, final Screen screen) {
        if (!ModuleManager.isModuleEnabled("PortalChat")) {
            minecraft.setScreen(screen);
        }
    }

    @Redirect(method = "handleNetherPortalClient", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;closeContainer()V"))
    private void xulu$portalChatCloseContainer(final LocalPlayer player) {
        if (!ModuleManager.isModuleEnabled("PortalChat")) {
            player.closeContainer();
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void xulu$offhandSwing(final InteractionHand hand, final CallbackInfo info) {
        final OffhandSwing module = (Xulu.MODULE_MANAGER == null) ? null : Xulu.MODULE_MANAGER.getModuleT(OffhandSwing.class);
        if (module == null || !module.isToggled()) {
            return;
        }
        ((LivingEntity)(Object)this).swing(InteractionHand.OFF_HAND, false);
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundSwingPacket(hand));
        }
        info.cancel();
    }
}
