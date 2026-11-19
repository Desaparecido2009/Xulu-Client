package com.elementars.eclient.mixin.mixins;

import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Timer.class)
public class MixinTimer
{
    @Shadow
    public float tickDelta;

    @Shadow
    public float partialTick;

    @Shadow
    private long lastMs;

    @Shadow
    @Final
    private float msPerTick;

    @Inject(method = "advanceTime", at = @At("HEAD"), cancellable = true)
    private void xulu$advanceTime(final long now, final CallbackInfoReturnable<Integer> info) {
        final float speed = com.elementars.eclient.module.misc.Timer.getSpeed();
        if (speed == 1.0f) {
            return;
        }
        final float adjustedMsPerTick = this.msPerTick / speed;
        this.tickDelta = (float)(now - this.lastMs) / adjustedMsPerTick;
        this.lastMs = now;
        this.partialTick += this.tickDelta;
        final int ticks = (int)this.partialTick;
        this.partialTick -= (float)ticks;
        info.setReturnValue(ticks);
    }
}
