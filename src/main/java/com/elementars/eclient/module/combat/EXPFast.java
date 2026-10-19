package com.elementars.eclient.module.combat;

import com.elementars.eclient.mixin.mixins.MixinMinecraft;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.Wrapper;
import net.minecraft.world.item.ExperienceBottleItem;

public class EXPFast extends Module
{
    public EXPFast() {
        super("EXPFast", "XP fast zoom", 0, Category.COMBAT, true);
    }

    @Override
    public void onUpdate() {
        if (!this.isToggled()) {
            return;
        }
        if (Wrapper.getMinecraft().player.getMainHandItem().getItem() instanceof ExperienceBottleItem) {
            ((MixinMinecraft)Wrapper.getMinecraft()).setRightClickDelay(0);
        }
    }
}
