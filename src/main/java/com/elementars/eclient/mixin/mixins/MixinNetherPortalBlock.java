package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.misc.AntiSound;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin({ NetherPortalBlock.class })
public class MixinNetherPortalBlock
{
    @Redirect(method = { "animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void xulu$portalSound(final Level level, final double x, final double y, final double z, final SoundEvent sound, final SoundSource source, final float volume, final float pitch, final boolean distanceDelay) {
        final AntiSound antiSound = Xulu.MODULE_MANAGER.getModuleT(AntiSound.class);
        if (antiSound != null && antiSound.isToggled() && antiSound.portal.getValue()) {
            return;
        }
        level.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
    }
}
