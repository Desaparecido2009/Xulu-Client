package com.elementars.eclient.module.player;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.Pair;
import dev.xulu.settings.Value;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoReplenish extends Module
{
    private final Value<String> mode;
    private final Value<Integer> threshold;
    private final Value<Integer> tickdelay;
    private int delay_step;

    public AutoReplenish() {
        super("AutoReplenish", "Automatically replenishes stacks in the hotbar", 0, Category.PLAYER, true);
        this.mode = this.register(new Value<String>("Mode", this, "All", new String[] { "All", "Crystals", "Xp", "Both" }));
        this.threshold = this.register(new Value<Integer>("Threshold", this, 32, 1, 63));
        this.tickdelay = this.register(new Value<Integer>("Delay", this, 2, 1, 10));
        this.delay_step = 0;
    }

    @Override
    public void onUpdate() {
        if (AutoReplenish.mc.player == null || AutoReplenish.mc.gameMode == null) {
            return;
        }
        if (AutoReplenish.mc.screen instanceof AbstractContainerScreen) {
            return;
        }
        if (AutoReplenish.mc.player.containerMenu != AutoReplenish.mc.player.inventoryMenu) {
            return;
        }
        if (this.delay_step < this.tickdelay.getValue()) {
            ++this.delay_step;
            return;
        }
        this.delay_step = 0;
        final Pair<Integer, Integer> slots = this.findReplenishableHotbarSlot();
        if (slots == null) {
            return;
        }
        final int containerId = AutoReplenish.mc.player.inventoryMenu.containerId;
        final int inventorySlot = slots.getKey();
        final int hotbarSlot = slots.getValue();
        AutoReplenish.mc.gameMode.handleInventoryMouseClick(containerId, inventorySlot, 0, ClickType.PICKUP, AutoReplenish.mc.player);
        AutoReplenish.mc.gameMode.handleInventoryMouseClick(containerId, hotbarSlot, 0, ClickType.PICKUP, AutoReplenish.mc.player);
        AutoReplenish.mc.gameMode.handleInventoryMouseClick(containerId, inventorySlot, 0, ClickType.PICKUP, AutoReplenish.mc.player);
    }

    private Pair<Integer, Integer> findReplenishableHotbarSlot() {
        Pair<Integer, Integer> returnPair = null;
        for (final Map.Entry<Integer, ItemStack> hotbarSlot : this.get_hotbar().entrySet()) {
            final ItemStack stack = hotbarSlot.getValue();
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.isStackable()) {
                continue;
            }
            if (!this.isTarget(stack)) {
                continue;
            }
            if (stack.getCount() >= stack.getMaxStackSize()) {
                continue;
            }
            if (stack.getCount() > this.threshold.getValue()) {
                continue;
            }
            final int inventorySlot = this.findCompatibleInventorySlot(stack);
            if (inventorySlot == -1) {
                continue;
            }
            returnPair = new Pair<Integer, Integer>(inventorySlot, hotbarSlot.getKey());
        }
        return returnPair;
    }

    private boolean isTarget(final ItemStack stack) {
        final String currentMode = this.mode.getValue();
        switch (currentMode) {
            case "Crystals": {
                return stack.getItem() == Items.END_CRYSTAL;
            }
            case "Xp": {
                return stack.getItem() == Items.EXPERIENCE_BOTTLE;
            }
            case "Both": {
                return stack.getItem() == Items.END_CRYSTAL || stack.getItem() == Items.EXPERIENCE_BOTTLE;
            }
            default: {
                return true;
            }
        }
    }

    private int findCompatibleInventorySlot(final ItemStack hotbarStack) {
        int inventorySlot = -1;
        int smallestStackSize = 999;
        for (final Map.Entry<Integer, ItemStack> entry : this.get_inventory().entrySet()) {
            final ItemStack inventoryStack = entry.getValue();
            if (inventoryStack.isEmpty()) {
                continue;
            }
            if (!this.isCompatibleStacks(hotbarStack, inventoryStack)) {
                continue;
            }
            final int currentStackSize = inventoryStack.getCount();
            if (smallestStackSize <= currentStackSize) {
                continue;
            }
            smallestStackSize = currentStackSize;
            inventorySlot = entry.getKey();
        }
        return inventorySlot;
    }

    private boolean isCompatibleStacks(final ItemStack stack1, final ItemStack stack2) {
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }

    private Map<Integer, ItemStack> get_inventory() {
        return this.get_inv_slots(9, 35);
    }

    private Map<Integer, ItemStack> get_hotbar() {
        return this.get_inv_slots(36, 44);
    }

    private Map<Integer, ItemStack> get_inv_slots(int current, final int last) {
        final Map<Integer, ItemStack> fullInventorySlots = new LinkedHashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, AutoReplenish.mc.player.inventoryMenu.getSlot(current).getItem());
            ++current;
        }
        return fullInventorySlots;
    }
}
