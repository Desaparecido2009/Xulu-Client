package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Obsidian extends Element
{
    public Obsidian() {
        super("Obsidian");
    }

    @Override
    public void onEnable() {
        this.width = 16.0;
        this.height = 16.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Obsidian.mc.player == null) {
            return;
        }
        int obsidian = Obsidian.mc.player.getInventory().items.stream().filter(itemStack -> itemStack.getItem() == Items.OBSIDIAN).mapToInt(ItemStack::getCount).sum();
        final ItemStack offhand = Obsidian.mc.player.getOffhandItem();
        if (offhand.getItem() == Items.OBSIDIAN) {
            obsidian += offhand.getCount();
        }
        final ItemStack items = new ItemStack(Items.OBSIDIAN, obsidian);
        Obsidian.mc.getItemRenderer().renderAndDecorateItem(items, (int)this.x, (int)this.y);
        Obsidian.mc.getItemRenderer().renderGuiItemDecorations(Obsidian.mc.font, items, (int)this.x, (int)this.y);
    }
}
