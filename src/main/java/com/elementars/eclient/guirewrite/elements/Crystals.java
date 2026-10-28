package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Crystals extends Element
{
    public Crystals() {
        super("Crystals");
    }

    @Override
    public void onEnable() {
        this.width = 16.0;
        this.height = 16.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Crystals.mc.player == null) {
            return;
        }
        int crystals = Crystals.mc.player.getInventory().items.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
        final ItemStack offhand = Crystals.mc.player.getOffhandItem();
        if (offhand.getItem() == Items.END_CRYSTAL) {
            crystals += offhand.getCount();
        }
        final ItemStack items = new ItemStack(Items.END_CRYSTAL, crystals);
        Crystals.mc.getItemRenderer().renderAndDecorateItem(items, (int)this.x, (int)this.y);
        Crystals.mc.getItemRenderer().renderGuiItemDecorations(Crystals.mc.font, items, (int)this.x, (int)this.y);
    }
}
