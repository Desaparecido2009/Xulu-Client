package com.elementars.eclient.mixin.mixins;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInHandRenderer.class)
public interface AccessorItemInHandRenderer
{
    @Accessor("mainHandHeight")
    float xulu$getMainHandHeight();

    @Accessor("mainHandHeight")
    void xulu$setMainHandHeight(final float height);

    @Accessor("mainHandItem")
    void xulu$setMainHandItem(final ItemStack stack);
}
