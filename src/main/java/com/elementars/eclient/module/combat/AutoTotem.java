package com.elementars.eclient.module.combat;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.LocalPlayerUpdateEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoTotem extends Module
{
    public final Value<Integer> health;
    public final Value<Integer> delayA;
    public final Value<Integer> delay;
    public static boolean switchInProgress;
    public static int offhand_delay;
    boolean didFirst;
    int switchDelay1;
    int switchDelay2;
    int slot;

    public AutoTotem() {
        super("AutoTotem", "Automatically places totems in your offhand", 0, Category.COMBAT, true);
        this.health = this.register(new Value<Integer>("Health", this, 20, 1, 22));
        this.delayA = this.register(new Value<Integer>("Delay", this, 1, 0, 5));
        this.delay = this.register(new Value<Integer>("Offhand Delay", this, 5, 0, 20));
        this.didFirst = false;
        this.switchDelay1 = -1;
        this.switchDelay2 = -1;
        this.slot = -1;
    }

    @Override
    public void onUpdate() {
        if (AutoTotem.switchInProgress) {
            return;
        }
        if (AutoTotem.mc.screen instanceof AbstractContainerScreen) {
            return;
        }
        if (AutoTotem.offhand_delay > 0) {
            --AutoTotem.offhand_delay;
        }
        if (this.shouldTotem() && (AutoTotem.mc.player.getOffhandItem() == ItemStack.EMPTY || AutoTotem.mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING)) {
            final int slot = (this.getTotemSlot() < 9) ? (this.getTotemSlot() + 36) : this.getTotemSlot();
            if (this.getTotemSlot() != -1) {
                this.slot = slot;
                AutoTotem.switchInProgress = true;
                AutoTotem.offhand_delay = this.delay.getValue();
            }
        }
    }

    @EventTarget
    public void onPlayerUpdate(final LocalPlayerUpdateEvent event) {
        if (AutoTotem.switchInProgress) {
            if (this.switchDelay1 > 0) {
                --this.switchDelay1;
            }
            if (this.switchDelay2 > 0) {
                --this.switchDelay2;
            }
            if (!this.didFirst) {
                AutoTotem.mc.gameMode.handleInventoryMouseClick(0, this.slot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                this.switchDelay1 = this.delayA.getValue();
                this.didFirst = true;
                return;
            }
            if (this.switchDelay1 == 0) {
                AutoTotem.mc.gameMode.handleInventoryMouseClick(0, 45, 0, ClickType.PICKUP, AutoTotem.mc.player);
                this.switchDelay1 = -1;
                this.switchDelay2 = this.delayA.getValue();
                return;
            }
            if (this.switchDelay2 == 0) {
                AutoTotem.mc.gameMode.handleInventoryMouseClick(0, this.slot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                this.switchDelay1 = -1;
                this.switchDelay2 = -1;
                this.slot = -1;
                AutoTotem.switchInProgress = false;
                this.didFirst = false;
            }
        }
    }

    private boolean shouldTotem() {
        return AutoTotem.mc.player.getHealth() + AutoTotem.mc.player.getAbsorptionAmount() <= (Xulu.MODULE_MANAGER.getModuleT(Offhand.class).conserveGapples.getValue() ? ((!Surround.isExposed() && isFullArmor(AutoTotem.mc.player)) ? 6 : this.health.getValue()) : this.health.getValue()) || AutoTotem.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    public static boolean isFullArmor(final Player entity) {
        boolean fullArmor = true;
        int diamondItems = 0;
        boolean hasBlast = false;
        for (final ItemStack is : entity.getArmorSlots()) {
            if (is.isEmpty()) {
                fullArmor = false;
                break;
            }
            if (is.getItem() == Items.DIAMOND_HELMET || is.getItem() == Items.NETHERITE_HELMET) {
                ++diamondItems;
            }
            if (is.getItem() == Items.DIAMOND_CHESTPLATE || is.getItem() == Items.NETHERITE_CHESTPLATE) {
                ++diamondItems;
            }
            if (is.getItem() == Items.DIAMOND_LEGGINGS || is.getItem() == Items.NETHERITE_LEGGINGS) {
                ++diamondItems;
            }
            if (is.getItem() == Items.DIAMOND_BOOTS || is.getItem() == Items.NETHERITE_BOOTS) {
                ++diamondItems;
            }
            final ListTag enchants = is.getEnchantmentTags();
            final List<String> enchantments = new ArrayList<String>();
            if (enchants != null) {
                for (int index = 0; index < enchants.size(); ++index) {
                    final CompoundTag tag = enchants.getCompound(index);
                    final String id = tag.getString("id");
                    final short level = tag.getShort("lvl");
                    final Enchantment enc = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(id));
                    if (enc != null) {
                        enchantments.add(enc.getFullname((int)level).getString());
                    }
                }
            }
            if (!enchantments.contains("Blast Protection IV")) {
                continue;
            }
            hasBlast = true;
        }
        return fullArmor && diamondItems == 4 && hasBlast;
    }

    int getTotemSlot() {
        int totemSlot = -1;
        for (int i = 45; i > -1; --i) {
            if (AutoTotem.mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        return totemSlot;
    }

    @Override
    public String getHudInfo() {
        int items = 0;
        for (int i = 45; i > -1; --i) {
            if (AutoTotem.mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                items += AutoTotem.mc.player.getInventory().getItem(i).getCount();
            }
        }
        return items + "";
    }

    static {
        AutoTotem.switchInProgress = false;
        AutoTotem.offhand_delay = 0;
    }
}
