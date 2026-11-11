package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class Gapples extends Element
{
    public Gapples() {
        super("Gapples");
    }

    @Override
    public void onEnable() {
        this.width = 16.0;
        this.height = 16.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Gapples.mc.player == null) {
            return;
        }
        int gapple = Gapples.mc.player.getInventory().items.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE || itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
        final ItemStack offhand = Gapples.mc.player.getOffhandItem();
        if (offhand.getItem() == Items.GOLDEN_APPLE || offhand.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            gapple += offhand.getCount();
        }
        final ItemStack items = new ItemStack(Items.GOLDEN_APPLE, gapple);
        if (gapple > 0) {
            items.enchant(Enchantments.SHARPNESS, 1);
        }
        Gapples.mc.getItemRenderer().renderAndDecorateItem(items, (int)this.x, (int)this.y);
        Gapples.mc.getItemRenderer().renderGuiItemDecorations(Gapples.mc.font, items, (int)this.x, (int)this.y);
    }
}
