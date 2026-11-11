package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.gui.ExitConfirmation;
import com.elementars.eclient.util.Wrapper;
import dev.xulu.settings.ConfigManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftStop
{
    @Inject(method = "stop", at = @At("HEAD"), cancellable = true)
    private void stopClient(final CallbackInfo callbackInfo) {
        final Minecraft mc = Minecraft.getInstance();
        if (ExitConfirmation.shouldConfirm(mc)) {
            callbackInfo.cancel();
            ExitConfirmation.show(mc);
            return;
        }
        if (mc.player != null) {
            ConfigManager.saveMain();
        }
        Wrapper.getFileManager().saveFriends();
        Wrapper.getFileManager().saveEnemies();
        Wrapper.getFileManager().saveHUD();
    }
}
