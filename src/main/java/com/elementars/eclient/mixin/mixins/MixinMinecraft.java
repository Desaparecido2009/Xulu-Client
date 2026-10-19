package com.elementars.eclient.mixin.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MixinMinecraft
{
    @Accessor("rightClickDelay")
    void setRightClickDelay(final int delay);
}
