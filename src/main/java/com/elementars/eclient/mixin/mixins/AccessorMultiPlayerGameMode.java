package com.elementars.eclient.mixin.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface AccessorMultiPlayerGameMode
{
    @Accessor("destroyDelay")
    void xulu$setDestroyDelay(int delay);

    @Accessor("destroyProgress")
    float xulu$getDestroyProgress();

    @Accessor("destroyProgress")
    void xulu$setDestroyProgress(float progress);

    @Accessor("isDestroying")
    boolean xulu$isDestroying();

    @Accessor("isDestroying")
    void xulu$setDestroying(boolean destroying);
}
