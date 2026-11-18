package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.misc.AntiSound;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ LocalPlayer.class })
public class MixinLocalPlayerSound
{
    @Redirect(method = { "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
    private void xulu$elytraSound(final SoundManager soundManager, final SoundInstance sound) {
        final AntiSound antiSound = Xulu.MODULE_MANAGER.getModuleT(AntiSound.class);
        if (antiSound != null && antiSound.isToggled() && antiSound.elytra.getValue()) {
            return;
        }
        soundManager.play(sound);
    }
}
