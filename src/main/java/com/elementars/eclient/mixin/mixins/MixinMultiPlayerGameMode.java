package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.EventAttackEntity;
import com.elementars.eclient.event.events.EventClickBlock;
import com.elementars.eclient.event.events.EventPlayerDamageBlock;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode
{
    @Inject(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void xulu$attack(final Player player, final Entity target, final CallbackInfo info) {
        new EventAttackEntity(target).call();
    }

    @Inject(method = "startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private void xulu$startDestroyBlock(final BlockPos pos, final Direction facing, final CallbackInfoReturnable<Boolean> info) {
        final EventClickBlock event = new EventClickBlock(pos, facing);
        event.call();
        if (event.isCancelled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private void xulu$continueDestroyBlock(final BlockPos pos, final Direction facing, final CallbackInfoReturnable<Boolean> info) {
        final EventPlayerDamageBlock event = new EventPlayerDamageBlock(pos, facing);
        event.call();
        if (event.isCancelled()) {
            info.setReturnValue(false);
        }
    }
}
