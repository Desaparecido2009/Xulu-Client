package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.event.events.EventMiddleClick;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Inject(method = "pickBlock", at = @At("HEAD"))
    private void xulu$middleClick(final CallbackInfo info) {
        new EventMiddleClick().call();
    }
}
