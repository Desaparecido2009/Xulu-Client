package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.misc.AntiSound;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ LevelRenderer.class })
public class MixinLevelRenderer
{
    @Redirect(method = { "levelEvent(Lnet/minecraft/world/entity/player/Player;ILnet/minecraft/core/BlockPos;I)V" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void xulu$witherShootSound(final ClientLevel level, final double x, final double y, final double z, final SoundEvent sound, final SoundSource source, final float volume, final float pitch, final boolean distanceDelay) {
        final AntiSound antiSound = Xulu.MODULE_MANAGER.getModuleT(AntiSound.class);
        if (sound == SoundEvents.WITHER_SHOOT && antiSound != null && antiSound.isToggled() && antiSound.wither.getValue()) {
            return;
        }
        level.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
    }
}
