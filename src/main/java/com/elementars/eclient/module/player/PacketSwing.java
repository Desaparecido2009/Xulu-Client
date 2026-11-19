package com.elementars.eclient.module.player;

import com.elementars.eclient.mixin.mixins.AccessorItemInHandRenderer;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class PacketSwing extends Module
{
    public PacketSwing() {
        super("PacketSwing", "Swings with packets lol", 0, Category.PLAYER, true);
    }

    @Override
    public void onUpdate() {
        if (PacketSwing.mc.player == null) {
            return;
        }
        final ItemStack held = PacketSwing.mc.player.getMainHandItem();
        if (!(held.getItem() instanceof SwordItem)) {
            return;
        }
        final AccessorItemInHandRenderer renderer = (AccessorItemInHandRenderer)PacketSwing.mc.getItemInHandRenderer();
        if (renderer.xulu$getMainHandHeight() < 0.9f) {
            return;
        }
        renderer.xulu$setMainHandHeight(1.0f);
        renderer.xulu$setMainHandItem(held);
    }
}
