package com.elementars.eclient.module.combat;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.Timer;
import dev.xulu.settings.Value;
import java.util.Arrays;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class AutoArmor extends Module
{
    private final Timer timer;
    private int[] bestArmorDamage;
    private int[] bestArmorSlots;
    private final Value<Boolean> pif;
    private final Value<Boolean> replace;
    private final Value<Boolean> preserve;
    private final Value<Integer> preserveDMG;
    private final Value<Integer> ms;

    public AutoArmor() {
        super("AutoArmor", "Automatically refills armor", 0, Category.COMBAT, true);
        this.timer = new Timer();
        this.pif = this.register(new Value<Boolean>("Pickup If Full", this, true));
        this.replace = this.register(new Value<Boolean>("Replace Empty", this, true));
        this.preserve = this.register(new Value<Boolean>("Preserve Damaged", this, false));
        this.preserveDMG = this.register(new Value<Integer>("Damage %", this, 5, 0, 100));
        this.ms = this.register(new Value<Integer>("MS delay", this, 500, 0, 1000));
    }

    @Override
    public void onUpdate() {
        if (AutoArmor.mc.screen instanceof AbstractContainerScreen && !(AutoArmor.mc.screen instanceof EffectRenderingInventoryScreen)) {
            return;
        }
        this.searchSlots();
        for (int i = 0; i < 4; ++i) {
            if (this.bestArmorSlots[i] != -1) {
                int bestSlot = this.bestArmorSlots[i];
                if (bestSlot < 9) {
                    bestSlot += 36;
                }
                if (!AutoArmor.mc.player.getInventory().armor.get(i).isEmpty()) {
                    if (AutoArmor.mc.player.getInventory().getFreeSlot() == -1 && !AutoTotem.switchInProgress && !Offhand.switchInProgress && this.pif.getValue()) {
                        AutoArmor.mc.gameMode.handleInventoryMouseClick(AutoArmor.mc.player.inventoryMenu.containerId, 8 - i, 0, ClickType.PICKUP, AutoArmor.mc.player);
                        AutoArmor.mc.gameMode.handleInventoryMouseClick(AutoArmor.mc.player.inventoryMenu.containerId, bestSlot, 0, ClickType.PICKUP, AutoArmor.mc.player);
                        AutoArmor.mc.gameMode.handleInventoryMouseClick(AutoArmor.mc.player.inventoryMenu.containerId, 8 - i, 0, ClickType.PICKUP, AutoArmor.mc.player);
                        continue;
                    }
                    AutoArmor.mc.gameMode.handleInventoryMouseClick(AutoArmor.mc.player.inventoryMenu.containerId, 8 - i, 0, ClickType.QUICK_MOVE, AutoArmor.mc.player);
                    if (!this.timer.hasReached(this.ms.getValue())) {
                        return;
                    }
                }
                AutoArmor.mc.gameMode.handleInventoryMouseClick(AutoArmor.mc.player.inventoryMenu.containerId, bestSlot, 0, ClickType.QUICK_MOVE, AutoArmor.mc.player);
                this.timer.reset();
            }
        }
    }

    private void searchSlots() {
        this.bestArmorDamage = new int[4];
        this.bestArmorSlots = new int[4];
        Arrays.fill(this.bestArmorDamage, -1);
        Arrays.fill(this.bestArmorSlots, -1);
        for (int i = 0; i < this.bestArmorSlots.length; ++i) {
            final ItemStack itemStack = AutoArmor.mc.player.getInventory().armor.get(i);
            if (itemStack.getItem() instanceof ArmorItem) {
                final ArmorItem armor = (ArmorItem)itemStack.getItem();
                if (this.preserve.getValue()) {
                    final float dmg = (itemStack.getMaxDamage() - (float)itemStack.getDamageValue()) / itemStack.getMaxDamage();
                    final int percent = (int)(dmg * 100.0f);
                    if (percent > this.preserveDMG.getValue()) {
                        this.bestArmorDamage[i] = armor.getDefense();
                    }
                }
                else {
                    this.bestArmorDamage[i] = armor.getDefense();
                }
            }
            else if (itemStack.isEmpty() && !this.replace.getValue()) {
                this.bestArmorDamage[i] = Integer.MAX_VALUE;
            }
        }
        for (int i = 0; i < 36; ++i) {
            final ItemStack itemStack = AutoArmor.mc.player.getInventory().getItem(i);
            if (itemStack.getCount() <= 1) {
                if (itemStack.getItem() instanceof ArmorItem) {
                    final ArmorItem armor = (ArmorItem)itemStack.getItem();
                    final int armorType = armor.getSlot().ordinal() - 2;
                    if (this.bestArmorDamage[armorType] < armor.getDefense()) {
                        this.bestArmorDamage[armorType] = armor.getDefense();
                        this.bestArmorSlots[armorType] = i;
                    }
                }
            }
        }
    }
}
