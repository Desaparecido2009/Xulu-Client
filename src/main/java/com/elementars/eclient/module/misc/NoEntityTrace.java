package com.elementars.eclient.module.misc;

import com.elementars.eclient.mixin.mixins.AccessorMultiPlayerGameMode;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;

public class NoEntityTrace extends Module
{
    private final Value<String> mode;
    private final Value<Boolean> pickaxe;
    private static NoEntityTrace INSTANCE;

    public NoEntityTrace() {
        super("NoEntityTrace", "Keeps mining through an entity", 0, Category.MISC, true);
        this.mode = this.register(new Value<String>("Mode", this, "Dynamic", new ArrayList<String>(Arrays.asList("Dynamic", "Static"))));
        this.pickaxe = this.register(new Value<Boolean>("Pickaxe Only", this, true));
        NoEntityTrace.INSTANCE = this;
    }

    public static boolean shouldBlock() {
        if (NoEntityTrace.INSTANCE == null || !NoEntityTrace.INSTANCE.isToggled()) {
            return false;
        }
        if (NoEntityTrace.mc.player == null || NoEntityTrace.mc.gameMode == null) {
            return false;
        }
        final boolean dynamic = NoEntityTrace.INSTANCE.mode.getValue().equalsIgnoreCase("Static")
            || ((AccessorMultiPlayerGameMode)NoEntityTrace.mc.gameMode).xulu$isDestroying();
        if (!dynamic) {
            return false;
        }
        if (!NoEntityTrace.INSTANCE.pickaxe.getValue()) {
            return true;
        }
        final ItemStack held = NoEntityTrace.mc.player.getMainHandItem();
        final Item item = held.getItem();
        return item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE;
    }
}
