package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.render.Cape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer
{
    @Inject(method = "getCloakTextureLocation", at = @At("HEAD"), cancellable = true)
    private void xulu$getCloakTextureLocation(final CallbackInfoReturnable<ResourceLocation> cir) {
        final AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
        if (Cape.isEnabled() && Minecraft.getInstance().player == player) {
            final ResourceLocation cape = Cape.getSelectedCape();
            if (cape != null) {
                cir.setReturnValue(cape);
            }
        }
    }

    @Inject(method = "isCapeLoaded", at = @At("HEAD"), cancellable = true)
    private void xulu$isCapeLoaded(final CallbackInfoReturnable<Boolean> cir) {
        final AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
        if (Cape.isEnabled() && Minecraft.getInstance().player == player) {
            cir.setReturnValue(true);
        }
    }
}
