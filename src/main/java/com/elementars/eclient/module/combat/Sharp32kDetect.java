package com.elementars.eclient.module.combat;

import net.minecraft.ChatFormatting;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class Sharp32kDetect extends Module
{
    private final Value<Boolean> watermark;
    private final Value<Boolean> color;
    private final Set<Player> sword;

    public Sharp32kDetect() {
        super("32kDetect", "Detects held 32ks", 0, Category.COMBAT, true);
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.color = this.register(new Value<Boolean>("Color", this, false));
        this.sword = Collections.newSetFromMap(new WeakHashMap<Player, Boolean>());
    }

    private boolean is32k(final ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItem)) {
            return false;
        }
        final ListTag enchantments = stack.getEnchantmentTags();
        for (int i = 0; i < enchantments.size(); ++i) {
            final CompoundTag tag = enchantments.getCompound(i);
            if (tag.getShort("lvl") >= 32767) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUpdate() {
        if (Sharp32kDetect.mc.level == null || Sharp32kDetect.mc.player == null) {
            return;
        }
        for (final Player player : Sharp32kDetect.mc.level.players()) {
            if (player == null || player == Sharp32kDetect.mc.player) {
                continue;
            }
            final String name = player.getDisplayName().getString();
            if (this.is32k(player.getMainHandItem()) && !this.sword.contains(player)) {
                this.sendMessage((this.color.getValue() ? ChatFormatting.DARK_RED.toString() : "") + name + " is holding a 32k");
                this.sword.add(player);
            }
            else if (this.sword.contains(player) && !this.is32k(player.getMainHandItem())) {
                this.sendMessage((this.color.getValue() ? ChatFormatting.DARK_GREEN.toString() : "") + name + " is no longer holding a 32k");
                this.sword.remove(player);
            }
        }
    }

    private void sendMessage(final String message) {
        if (this.watermark.getValue()) {
            Command.sendChatMessage(message);
        }
        else {
            Command.sendRawChatMessage(message);
        }
    }
}
