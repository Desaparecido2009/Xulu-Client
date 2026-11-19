package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.EventAttackEntity;
import com.elementars.eclient.event.events.EventClickBlock;
import com.elementars.eclient.event.events.EventPlayerDamageBlock;
import com.elementars.eclient.module.player.TpsSync;
import com.elementars.eclient.util.LagCompensator;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float xulu$tpsSync(final BlockState state, final Player player, final BlockGetter level, final BlockPos pos) {
        final float multiplier = (TpsSync.isSync() && LagCompensator.INSTANCE != null) ? (LagCompensator.INSTANCE.getTickRate() / 20.0f) : 1.0f;
        return state.getDestroyProgress(player, level, pos) * multiplier;
    }
}
