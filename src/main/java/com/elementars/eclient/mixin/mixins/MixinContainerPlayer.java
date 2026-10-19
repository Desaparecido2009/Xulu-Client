package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.CloseInventoryEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public class MixinContainerPlayer
{
    @Inject(method = "removed(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
    public void xulu$onContainerClosed(final Player playerIn, final CallbackInfo ci) {
        final CloseInventoryEvent event = new CloseInventoryEvent();
        event.call();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
