package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.module.ModuleManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera
{
    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void xulu$cameraClip(final double distance, final CallbackInfoReturnable<Double> info) {
        if (ModuleManager.isModuleEnabled("CameraClip")) {
            info.setReturnValue(distance);
        }
    }
}
