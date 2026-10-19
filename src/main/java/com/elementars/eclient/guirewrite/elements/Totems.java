package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Totems extends Element
{
    public Totems() {
        super("Totems");
    }

    @Override
    public void onEnable() {
        this.width = 16.0;
        this.height = 16.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Totems.mc.player == null) {
            return;
        }
        int totems = Totems.mc.player.getInventory().items.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (Totems.mc.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
            totems += Totems.mc.player.getOffhandItem().getCount();
        }
        final ItemStack items = new ItemStack(Items.TOTEM_OF_UNDYING, totems);
        Totems.mc.getItemRenderer().renderAndDecorateItem(items, (int)this.x, (int)this.y);
        Totems.mc.getItemRenderer().renderGuiItemDecorations(Totems.fontRenderer, items, (int)this.x, (int)this.y);
    }
}
