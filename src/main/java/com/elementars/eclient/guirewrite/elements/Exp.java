package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Exp extends Element
{
    public Exp() {
        super("Exp");
    }

    @Override
    public void onEnable() {
        this.width = 16.0;
        this.height = 16.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (Exp.mc.player == null) {
            return;
        }
        int exp = Exp.mc.player.getInventory().items.stream().filter(itemStack -> itemStack.getItem() == Items.EXPERIENCE_BOTTLE).mapToInt(ItemStack::getCount).sum();
        final ItemStack offhand = Exp.mc.player.getOffhandItem();
        if (offhand.getItem() == Items.EXPERIENCE_BOTTLE) {
            exp += offhand.getCount();
        }
        final ItemStack items = new ItemStack(Items.EXPERIENCE_BOTTLE, exp);
        Exp.mc.getItemRenderer().renderAndDecorateItem(items, (int)this.x, (int)this.y);
        Exp.mc.getItemRenderer().renderGuiItemDecorations(Exp.mc.font, items, (int)this.x, (int)this.y);
    }
}
