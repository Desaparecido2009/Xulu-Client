package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.util.Wrapper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftStop
{
    @Inject(method = "stop", at = @At("HEAD"))
    private void stopClient(final CallbackInfo callbackInfo) {
        Wrapper.getFileManager().saveFriends();
        Wrapper.getFileManager().saveEnemies();
        Wrapper.getFileManager().saveHUD();
    }
}
